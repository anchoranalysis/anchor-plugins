/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.image.task.bean.scale;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.spatial.arrange.align.Align;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.bean.stack.metadata.reader.ImageMetadataReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.image.task.size.CommonSize;
import org.anchoranalysis.plugin.image.task.size.SizeMapping;
import org.anchoranalysis.plugin.image.task.stack.ImageSizePrereader;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.scale.RelativeScaleCalculator;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Creates a scaled copy of images, ensuring all images have identical output size.
 *
 * <p>When given input images of either varying or identical size, it will produce outputs of
 * identical (scaled) size.
 *
 * <p>Whenever the scaled image is smaller than the output size, it is aligned (e.g. centered) and
 * unused pixels are assigned the background color of 0.
 *
 * @author Owen Feehan
 */
public class ScaleImageCommonSize extends ScaleImage<CommonSize> {

    // START BEAN PROPERTIES
    /** How to read the {@link ImageMetadata} from the file-system. */
    @DefaultInstance @BeanField @Getter @Setter private ImageMetadataReader imageMetadataReader;

    /**
     * Fallback for {@code imageMetadataReader} to read image files without a direct metadata
     * reader.
     */
    @DefaultInstance @BeanField @Getter @Setter private StackReader stackReader;

    /**
     * How a smaller image (after scaling) is aligned to the larger image (with the common size).
     *
     * <p>e.g. centered, left-aligned etc. in each dimension.
     */
    @BeanField @Getter @Setter private Align align = new Align();

    /**
     * The background intensity to use in each channel when the scaled image does not cover the
     * entire canvas.
     *
     * <p>0 would create black, 255 would create white.
     */
    @BeanField @Getter @Setter private int backgroundIntensity = 0;

    // END BEAN PROPERTIES

    @Override
    public CommonSize beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<StackSequenceInput> inputs,
            ParametersExperiment parameters)
            throws ExperimentExecutionException {

        OperationContext context = parameters.getContext().operationContext();

        ImageSizePrereader prereader =
                new ImageSizePrereader(imageMetadataReader, stackReader, context);

        try {
            List<SizeMapping> imageSizes = prereader.imageSizesFor(inputs);
            Extent maxSize =
                    DeriveCommonSize.scaleAndFindMax(
                            imageSizes,
                            mapping ->
                                    scaleMapping(
                                            mapping,
                                            parameters
                                                    .deriveInitializationContext()
                                                    .getSuggestedSize()));
            return new CommonSize(maxSize);
        } catch (OperationFailedException e) {
            throw new ExperimentExecutionException("An error occurred scaling the image-sizes", e);
        }
    }

    @Override
    protected Stack scaleStack(
            Stack stack,
            Optional<ImageSizeSuggestion> suggestedSize,
            VoxelsResizer voxelsResizer,
            CommonSize sharedState)
            throws OperationFailedException {
        Stack scaled = scaleStack(stack, sharedState.extent(), voxelsResizer);

        if (scaled.dimensions().extent().equals(sharedState.extent())) {
            // Nothing to do as the scaled size, is the same as our desired common size
            return scaled;
        } else {
            // Create new stack, and insert the scaled stack into it
            return scaled.mapChannel(channel -> alignChannelInto(channel, sharedState.extent()));
        }
    }

    private Channel alignChannelInto(Channel smaller, Extent commonSize)
            throws OperationFailedException {
        Dimensions dimensions = new Dimensions(commonSize, smaller.dimensions().resolution());
        Channel larger = ChannelFactory.instance().create(dimensions, smaller.getVoxelDataType());

        if (backgroundIntensity != 0) {
            larger.assignValue(backgroundIntensity);
        }

        BoundingBox box = align.align(smaller.extent(), larger.extent());
        smaller.voxels().copyVoxelsTo(new BoundingBox(smaller.extent()), larger.voxels(), box);
        return larger;
    }

    /** Scale an individual mapping. */
    private SizeMapping scaleMapping(
            SizeMapping mapping, Optional<ImageSizeSuggestion> suggestedSize)
            throws OperationFailedException {
        ScaleFactor scaleFactor =
                scaleCalculator.calculate(
                        Optional.of(new Dimensions(mapping.getExtent())), suggestedSize);
        return mapping.scaleXYBy(scaleFactor);
    }

    private Stack scaleStack(Stack stack, Extent maxSize, VoxelsResizer voxelsResizer)
            throws OperationFailedException {
        ScaleFactor scaleFactor =
                RelativeScaleCalculator.relativeScale(stack.dimensions().extent(), maxSize, true);
        return stack.mapChannel(
                channel -> scaleChannel(channel, binary, scaleFactor, voxelsResizer));
    }

    private static Channel scaleChannel(
            Channel channel, boolean binary, ScaleFactor scaleFactor, VoxelsResizer voxelsResizer)
            throws OperationFailedException {
        return ScaleChannelHelper.scaleChannel(channel, binary, scaleFactor, voxelsResizer);
    }
}
