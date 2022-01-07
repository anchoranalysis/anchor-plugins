/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.bean.object.segment.channel;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedUnaryOperator;
import org.anchoranalysis.image.bean.interpolator.Interpolator;
import org.anchoranalysis.image.bean.nonbean.segment.SegmentationFailedException;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjectsUnary;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.image.core.object.scale.Scaler;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Perform a segmentation at a different scale, and then fit the results back to the original scale.
 *
 * @author Owen Feehan
 */
public class AtScale extends SegmentChannelIntoObjectsUnary {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ScaleCalculator scaleCalculator;

    @BeanField @Getter @Setter private int outlineWidth = 1;

    /** The interpolator to use. */
    @BeanField @Getter @Setter @DefaultInstance private Interpolator interpolator;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection segment(
            Channel channel,
            Optional<ObjectMask> objectMask,
            Optional<ObjectCollection> seeds,
            SegmentChannelIntoObjects upstreamSegmenter)
            throws SegmentationFailedException {
        try {
            ScaleFactor scaleFactor =
                    determineScaleFactor(
                            channel.dimensions(), getInitialization().suggestedResize());

            Extent extent = channel.extent();

            // Perform segmentation on scaled versions of the channel, mask and seeds
            ObjectCollection scaledSegmentationResult =
                    upstreamSegmenter.segment(
                            scaleChannel(channel, scaleFactor, interpolator.voxelsResizer()),
                            scaleMask(objectMask, scaleFactor, extent),
                            scaleSeeds(seeds, scaleFactor, extent));

            // Segment and scale results back up to original-scale
            return scaleResultToOriginalScale(
                    scaledSegmentationResult, scaleFactor, channel.dimensions().extent());
        } catch (OperationFailedException | InitializeException e) {
            throw new SegmentationFailedException(e);
        }
    }

    private Channel scaleChannel(Channel channel, ScaleFactor scaleFactor, VoxelsResizer resizer) {
        return channel.scaleXY(scaleFactor, resizer);
    }

    private Optional<ObjectMask> scaleMask(
            Optional<ObjectMask> objectMask, ScaleFactor scaleFactor, Extent extent)
            throws SegmentationFailedException {

        return mapScale(
                objectMask, object -> object.scale(scaleFactor, Optional.of(extent)), "mask");
    }

    private Optional<ObjectCollection> scaleSeeds(
            Optional<ObjectCollection> seeds, ScaleFactor scaleFactor, Extent extent)
            throws SegmentationFailedException {
        return mapScale(
                seeds, seedCollection -> scaleSeeds(seedCollection, scaleFactor, extent), "seeds");
    }

    private ScaleFactor determineScaleFactor(
            Dimensions dimensions, Optional<ImageSizeSuggestion> suggestedResize)
            throws SegmentationFailedException {
        try {
            return scaleCalculator.calculate(Optional.of(dimensions), suggestedResize);
        } catch (OperationFailedException e) {
            throw new SegmentationFailedException("Cannot calculate scale", e);
        }
    }

    private ObjectCollection scaleResultToOriginalScale(
            ObjectCollection objects, ScaleFactor scaleFactor, Extent originalExtent)
            throws OperationFailedException {
        return ObjectCollectionFactory.of(
                Scaler.scaleObjects(objects, scaleFactor.invert(), true, originalExtent)
                        .asCollectionOrderNotPreserved());
    }

    /**
     * Scales an {@link Optional} if its present
     *
     * @param <T> optional-type
     * @param optional the optional to be scaled
     * @param scaleFunction function to use for scaling
     * @param textualDescriptionInError how to describe the optional in an error message
     * @return an optional with either a scaled value or empty() depending on the input-option
     * @throws SegmentationFailedException if the scaling operation fails
     */
    private static <T> Optional<T> mapScale(
            Optional<T> optional,
            CheckedUnaryOperator<T, OperationFailedException> scaleFunction,
            String textualDescriptionInError)
            throws SegmentationFailedException {
        try {
            if (optional.isPresent()) {
                return Optional.of(scaleFunction.apply(optional.get()));
            } else {
                return Optional.empty();
            }
        } catch (OperationFailedException e) {
            throw new SegmentationFailedException("Cannot scale " + textualDescriptionInError);
        }
    }

    private static ObjectCollection scaleSeeds(
            ObjectCollection seedsUnscaled, ScaleFactor scaleFactor, Extent extent)
            throws OperationFailedException {

        if (scaleFactor.x() != scaleFactor.y()) {
            throw new OperationFailedException(
                    "scaleFactor in X and Y must be equal to scale seeds");
        }

        return seedsUnscaled.stream().map(object -> object.scale(scaleFactor, Optional.of(extent)));
    }
}
