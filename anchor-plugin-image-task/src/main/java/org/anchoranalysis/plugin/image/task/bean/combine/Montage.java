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
import org.anchoranalysis.core.progress.ProgressIgnore;
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
import org.anchoranalysis.image.bean.spatial.arrange.fill.Fill;
import org.anchoranalysis.image.bean.spatial.arrange.tile.Tile;
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
import org.anchoranalysis.plugin.image.bean.scale.ToSuggested;
import org.anchoranalysis.plugin.image.task.slice.MontageSharedState;

/**
 * Creates a montage of images, by tiling them side-by-side.
 *
 * <p>The images are tiled into a grid formation, to have a approximately similar number of rows and
 * columns.
 *
 * <p>By default, each image will be scaled to approximately 600x480 (but usually not exactly this,
 * to preserve aspect ratio, and fill available space).
 *
 * <p>Any size suggestion passed via arguments will take priority over this default if set, or any
 * bean assigned to {@code scale} (in that order of precedence).
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

    /**
     * Number of pixels <i>width</i> to scale an image to approximately, if no alternative is
     * provided in {@code scale}.
     */
    private static final int DEFAULT_WIDTH_SCALE = 600;

    /**
     * Number of pixels <i>height</i> to scale an image to approximately, if no alternative is
     * provided in {@code scale}.
     */
    private static final int DEFAULT_HEIGHT_SCALE = 480;

    // START BEAN PROPERTIES
    /** How to read the {@link ImageMetadata} from the file-system. */
    @DefaultInstance @BeanField @Getter @Setter private ImageMetadataReader imageMetadataReader;

    /**
     * Fallback for {@code imageMetadataReader} to read image files without a directy metadata
     * reader.
     */
    @DefaultInstance @BeanField @Getter @Setter private StackReader stackReader;

    /** How much to scale each image by, before fitting to the montage. */
    @BeanField @Getter @Setter private ScaleCalculator scale = defaultScaleCalculator();

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
     * its asosicated image.
     *
     * <p>By default, it is horizontally-centered at the bottom of the image.
     */
    @BeanField @Getter @Setter
    private BoxAligner alignerLabel = new Align("center", "bottom", "bottom");
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
                new ImageSizePrereader(
                        scale,
                        imageMetadataReader,
                        stackReader,
                        parameters.getExperimentArguments().task().getSize(),
                        context);

        return MontageSharedStateFactory.create(
                prereader.deriveSizeForAllInputs(inputs),
                createArranger(inputs.size()),
                interpolator.voxelsResizer(),
                context);
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
                                                    path,
                                                    stackLabel));

        } catch (InputReadFailedException | OperationFailedException e) {
            throw new JobExecutionException(
                    "An error occurred copying the input image into the montage: "
                            + input.getInput().identifier(),
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

    /** Creates the {@link StackArranger} that will determine the tabular pattern. */
    private StackArranger createArranger(int numberInputs) {

        // Determine number of rows, so to give a similar number of rows and columns (as close to a
        // square as possible, ignoring the aspectRatio).
        int rows = (int) Math.ceil(Math.sqrt(numberInputs));

        if (varyImageSize) {
            // Completely fill space, allowing for a different number of rows and columns
            Fill fill = new Fill();
            fill.setNumberRows(rows);
            fill.setVaryNumberImagesPerRow(varyImageLocation);
            return fill;
        } else {

            int columns = (int) Math.ceil(((double) numberInputs) / rows);

            // A strictly tabular form, where each image must fit inside its cell-size.
            Tile tile = new Tile();
            tile.setNumberColumns(columns);
            tile.setNumberRows(rows);
            tile.setAligner(aligner);
            return tile;
        }
    }

    /** Is labelling enabled as an output? */
    private boolean labelsEnabled(Outputter outputter) {
        return outputter.outputsEnabled().isOutputEnabled(OUTPUT_LABELLED);
    }

    /** Reads a {@link Stack} to montage from the input. */
    private Stack readStackFromInput(InputBound<StackSequenceInput, MontageSharedState> input)
            throws InputReadFailedException {
        try {
            Stack stack = input.getInput().asStack(ProgressIgnore.get(), input.getLogger()).projectMax();
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

    /** The default {@link ScaleCalculator} if no other is provided. */
    private static ScaleCalculator defaultScaleCalculator() {
        ScaleCalculator fallback =
                new ToDimensions(DEFAULT_WIDTH_SCALE, DEFAULT_HEIGHT_SCALE, true);
        return new ToSuggested(fallback);
    }
}
