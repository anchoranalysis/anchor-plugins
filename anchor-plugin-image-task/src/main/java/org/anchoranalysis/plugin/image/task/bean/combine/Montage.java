/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.bean.combine;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalFactory;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.interpolator.Interpolator;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.bean.spatial.arrange.StackArranger;
import org.anchoranalysis.image.bean.spatial.arrange.align.Align;
import org.anchoranalysis.image.bean.spatial.arrange.align.BoxAligner;
import org.anchoranalysis.image.bean.spatial.arrange.align.Grow;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.bean.stack.metadata.reader.ImageMetadataReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.image.io.stack.output.generator.StackGenerator;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.io.output.writer.WriterRouterErrors;
import org.anchoranalysis.plugin.image.bean.scale.ToDimensions;
import org.anchoranalysis.plugin.image.task.slice.MontageSharedState;

/**
 * Creates a montage of images, by tiling them side-by-side.
 *
 * <p>The images are tiled into a grid formation, to have a approximately similar number of rows and
 * columns.
 *
 * <p>The size of the montage is determined by two factors, {@code varyImageSize} and an optional
 * suggestion on size.
 *
 * <p>When {@code varyImageSize==false}, by default each image will be scaled to approximately
 * 600x480, preserving aspect ratio, as per {@code fixedSizeScaler}. However, a size suggestion in
 * the form of a uniform scaling constant, will override this, and be applied instead to each image.
 * Other size suggestions are disallowed.
 *
 * <p>When {@code varyImageSize==true}, by default the combined image will have the larger of {@code
 * varyingSizeWidth} and {@code varyingSizeWidthRatio} (as calculated against the average row-size).
 * However, suggestions offering a fixed-width (but no height should be specified), or a constant
 * scaling-factor will override this. Other size suggestions are disallowed.
 *
 * <p>Any 3D images are flattened into a 2D image using a maximum-intensity projection.
 *
 * <p>The montage occurs in a two step process:
 *
 * <ul>
 *   <li><i>Sequentially</i>, the metadata of all images is initially read, so that widths and
 *       heights are known, and an arrangement can be determined. This occurs through the {@code
 *       imageMetadataReader} which is often much quicker than opening an image with the {@code
 *       stackReader} but this is not always the case.
 *   <li>Then in <i>parallel</i> each image is read from the file-system and added to the combined
 *       image.
 * </ul>
 *
 * <p>If a particular image is errored during the first step, it is omitted from the montage
 * entirely, and an error will register with its respective job in the second step.
 *
 * <p>If a particular image succeeds in the first step, but fails in the second step, an error will
 * register with the respective job, and its place on the montage will be entirely black - apart
 * from a label of background red color, if labelling is enabled.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>{@value Montage#OUTPUT_UNLABELLED}</td><td>yes</td><td>The montage of all the input images - <b>without</b> a label indicating the identifier of each image.</td></tr>
 * <tr><td>{@value Montage#OUTPUT_LABELLED}</td><td>yes</td><td>The montage of all the input images - <b>with</b> a label indicating the identifier of each image.</td></tr>
 * <tr><td rowspan="3"><i>inherited from {@link Task}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 */
public class Montage extends Task<StackSequenceInput, MontageSharedState> {

    /** The combined version of the stacks - without a label. */
    static final String OUTPUT_UNLABELLED = "unlabelled";

    /** The combined version of the stacks - with a label. */
    static final String OUTPUT_LABELLED = "labelled";

    // START BEAN PROPERTIES
    /** How to read the {@link ImageMetadata} from the file-system. */
    @DefaultInstance @BeanField @Getter @Setter private ImageMetadataReader imageMetadataReader;

    /**
     * Fallback for {@code imageMetadataReader} to read image files without a directy metadata
     * reader.
     */
    @DefaultInstance @BeanField @Getter @Setter private StackReader stackReader;

    /** How to resize images. */
    @DefaultInstance @BeanField @Getter @Setter private Interpolator interpolator;

    /**
     * When true, the images may vary in width/height in their respective rows to fill space, while
     * preserving the aspect-ratio of each image.
     */
    @BeanField @Getter @Setter private boolean varyImageSize = true;

    /**
     * When true, the location of an image in the grid, as well as the number of images in each row
     * are both allowed to vary to fill space.
     *
     * <p>When true, {@code varyImageSize} will always be considered also as {@code true}.
     */
    @BeanField @Getter @Setter private boolean varyImageLocation = true;

    /**
     * When {@code varyImageSize==false} and {@code varyImageLocation==false}, how to align a
     * smaller image inside a larger cell. Otherwise ignored.
     *
     * <p>By default, the smaller image grows as much as possible, while preserving the
     * aspect-ratio, but while strictly keeping a tabular form.
     */
    @BeanField @Getter @Setter private BoxAligner aligner = new Grow(true);

    /**
     * When {@code label==true}, this determines the height of the label.
     *
     * <p>Otherwise, it is ignored.
     *
     * <p>It indicates what portion of the average-image-height (when projected into the image)
     * should the label approximately occupy.
     *
     * <p>It defaults to {@code 0.05} i.e. the label should typically occupy 5% of the average
     * image-height.
     *
     * <p>It can be adjusted to make the label larger or smaller, relative to the image that is
     * being labelled.
     *
     * <p>Note that a lower minimum exists of label font-size, below which it will not become
     * smaller.
     */
    @BeanField @Getter @Setter @Positive private double ratioHeightForLabel = 0.05;

    /**
     * When {@code label==false} and {@code varyImageLocation==false}, how to align the label with
     * its associated image.
     *
     * <p>By default, it is horizontally-centered at the bottom of the image.
     */
    @BeanField @Getter @Setter
    private BoxAligner alignerLabel = new Align("center", "bottom", "bottom");

    /**
     * How to calculate the size of each image, when {@code varyImageSize==false}.
     *
     * <p>Otherwise, it is irrelevant.
     */
    @BeanField @Getter @Setter
    private ScaleCalculator fixedSizeScaler = new ToDimensions(600, 480, true);

    /**
     * If no specific width or scaling-factor is suggested, this determines the default width that
     * the combined-montage should have, when {@code varyImageSize==true}.
     *
     * <p>The eventual width will be the maximum of this and the width calculated from {@code
     * varyingSizeWidthRatio}.
     */
    @BeanField @Getter @Setter private int varyingSizeWidth = 1024;

    /**
     * If no specific width or scaling-factor is suggested, this determines the default percentage
     * of the existing size, the combined-montage should have, when {@code varyImageSize==true}.
     *
     * <p>The eventual width will be the maximum of this and {@code varyingSizeWidth}.
     */
    @BeanField @Getter @Setter private double varyingSizeWidthRatio = 0.1;
    
    /**
     * An ideal approximate ratio of the number of rows to the number of columns.
     * 
     * <p>When {@code == 1.0}, then the algorithm tries to have approximately <i>the same number of rows as columns</i>.
     * 
     * <p>When {@code > 1.0), then the algorithm tries to have <i>more rows than columns</i>, to match the ratio {@code number_rows / number_columns}.
     * 
     * <p>When {@code < 1.0), then the algorithm tries to have <i>more columns than rows</i>, to match the ratio {@code number_rows / number_columns}.
     */
    @BeanField @Getter @Setter private double ratioRowsToColumns = 1.0;
    // END BEAN PROPERTIES

    @Override
    public MontageSharedState beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<StackSequenceInput> inputs,
            ParametersExperiment parameters)
            throws ExperimentExecutionException {

        if (inputs.isEmpty()) {
            throw new ExperimentExecutionException("No inputs exist, so no montage can be created");
        }

        OperationContext context = parameters.getContext().operationContext();

        ImageSizePrereader prereader =
                new ImageSizePrereader(imageMetadataReader, stackReader, context);

        List<SizeMapping> imageSizes = prereader.imageSizesFor(inputs);

        try {
            StackArranger arranger =
                    createArranger(
                            imageSizes, parameters.getExperimentArguments().task().getSize());
            return MontageSharedStateFactory.create(
                    imageSizes, arranger, interpolator.voxelsResizer(), context);
        } catch (OperationFailedException e) {
            throw new ExperimentExecutionException(
                    "An error occurred arranging the images in the montage", e);
        }
    }

    @Override
    public void doJobOnInput(InputBound<StackSequenceInput, MontageSharedState> input)
            throws JobExecutionException {

        try {
            Optional<String> stackLabel =
                    OptionalFactory.create(
                            labelsEnabled(input.getOutputter()), input.getInput()::identifier);

            Path path = input.getInput().pathForBindingRequired();
            input.getContextExperiment()
                    .getExecutionTimeRecorder()
                    .recordExecutionTime(
                            "Copy and scale stack into montage",
                            () ->
                                    input.getSharedState()
                                            .copyStackInto(
                                                    () -> readStackFromInput(input),
                                                    input.getInput().identifier(),
                                                    path,
                                                    stackLabel));

        } catch (InputReadFailedException e) {
            throw new JobExecutionException(
                    String.format(
                            "An error occurred copying the input image into the montage: %s",
                            input.getInput().identifier()),
                    e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(MontageSharedState sharedState, InputOutputContext context)
            throws ExperimentExecutionException {

        writeMontage(context.getOutputter().writerSelective(), OUTPUT_UNLABELLED, sharedState);

        try {
            if (labelsEnabled(context.getOutputter())) {

                context.getExecutionTimeRecorder()
                        .recordExecutionTime(
                                "Draw all labels",
                                () -> sharedState.drawAllLabels(ratioHeightForLabel, alignerLabel));
                writeMontage(
                        context.getOutputter().writerPermissive(), OUTPUT_LABELLED, sharedState);
            }
        } catch (OperationFailedException e) {
            throw new ExperimentExecutionException(
                    "A problem occurred drawing labels on the montaged image", e);
        }
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(StackSequenceInput.class);
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs().addEnabledOutputFirst(OUTPUT_LABELLED);
    }

    /**
     * Creates the {@link StackArranger} that will determine the tabular pattern in the montage.
     *
     * @param imageSizes the size of each image to arrange. This list may or may not be be modified
     *     by this function, to change the sizes, if desired for subsequent processing. If changed,
     *     the order of each element remains invariant, as well as the size of the list.
     * @param suggestedSize any suggestion passed by the user for how large the montage should be.
     * @return the newly created {@link StackArranger}.
     * @throws OperationFailedException
     */
    private StackArranger createArranger(
            List<SizeMapping> imageSizes, Optional<ImageSizeSuggestion> suggestedSize)
            throws OperationFailedException {

        int numberRows = calculateNumberRows(imageSizes.size());

        if (varyImageSize) {
            return new VaryingImageSizeArranger(varyingSizeWidth, varyingSizeWidthRatio)
                    .create(numberRows, suggestedSize, varyImageLocation);
        } else {
            return new FixedImageSizeArranger(fixedSizeScaler, aligner)
                    .create(numberRows, suggestedSize, imageSizes);
        }
    }
    
    /** 
     * Determine the number of rows.
     * 
     * <p>This occurs so that {@code number_rows / number_columns}, approximately matches {@code ratioRowsToColumns}.
     * 
     * See the javadoc documentation for {@code ratioRowsToColumns} to understand its influence.
     *
     * @param numberImagesToArrange the total number of images to arrange in a table, with rows and columns.
     * @return the number of rows to use.
     */
    private int calculateNumberRows(int numberImagesToArrange) {
    	return (int) Math.ceil(Math.sqrt(numberImagesToArrange) * ratioRowsToColumns);
    }

    /** Is labelling enabled as an output? */
    private boolean labelsEnabled(Outputter outputter) {
        return outputter.outputsEnabled().isOutputEnabled(OUTPUT_LABELLED);
    }

    /** Reads a {@link Stack} to montage from the input. */
    private Stack readStackFromInput(InputBound<StackSequenceInput, MontageSharedState> input)
            throws InputReadFailedException {
        try {
            Stack stack = input.getInput().asStack(input.getLogger()).projectMax();
            return DisplayStack.create(stack.extractUpToThreeChannels()).deriveStack(false);
        } catch (OperationFailedException | CreateException e) {
            throw new InputReadFailedException(
                    "Cannot extract a stack representation from the input", e);
        }
    }

    /** Write the montaged image to the file-system */
    private void writeMontage(
            WriterRouterErrors writer, String outputName, MontageSharedState sharedState) {
        writer.write(outputName, () -> new StackGenerator(true), sharedState.getStack()::asStack);
    }
}
