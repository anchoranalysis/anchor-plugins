package org.anchoranalysis.plugin.image.task.bean.slice.combine;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.CheckedStream;
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
import org.anchoranalysis.image.bean.spatial.arrange.align.BoxAligner;
import org.anchoranalysis.image.bean.spatial.arrange.align.Grow;
import org.anchoranalysis.image.bean.spatial.arrange.tile.Tile;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.metadata.reader.ImageMetadataReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.image.io.stack.output.generator.StackGenerator;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.image.bean.scale.ToDimensions;
import org.anchoranalysis.plugin.image.bean.scale.ToSuggested;
import org.anchoranalysis.plugin.image.task.bean.scale.ScaleImage;
import org.anchoranalysis.plugin.image.task.slice.MontageSharedState;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Creates a montage of images, by tiling them side-by-side.
 *
 * <p>The images are tiled into a grid formation, to have a approximately similar number of rows and
 * columns.
 *
 * <p>By default, each image will be scaled to approximately 600x480 (but usually not exactly this,
 * to preserve aspect ratio, and fill available space).
 *
 * <p>The {@link TaskArguments#size} will take priority over this default if set, or any bean
 * assigned to {@code scale}.
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
 *   <li>Then in <i>parallel</b> each image is read from the file-system and added to the combined
 *       image.
 * </ul>
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>{@value ScaleImage#OUTPUT_MONTAGE}</td><td>yes</td><td>The montage of all the input images.</td></tr>
 * <tr><td rowspan="3"><i>inherited from {@link Task}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 */
public class Montage extends Task<StackSequenceInput, MontageSharedState> {

    /** The combined version of the stacks. */
    private static final String OUTPUT_MONTAGE = "montage";

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

    /** How to align a smaller image inside a larger cell. */
    @BeanField @Getter @Setter private BoxAligner aligner = new Grow(true);
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

        StackArranger arranger = createArranger(inputs.size());

        try {
            // The binding-paths for all the inputs
            Stream<Path> paths =
                    CheckedStream.map(
                            inputs.stream(),
                            InputReadFailedException.class,
                            StackSequenceInput::pathForBindingRequired);

            // Create the shared-state
            Optional<ImageSizeSuggestion> suggestedSize =
                    parameters.getExperimentArguments().task().getSize();
            OperationContext context = parameters.getContext().operationContext();
            return MontageSharedStateFactory.create(
                    paths,
                    arranger,
                    interpolator.voxelsResizer(),
                    path -> scaledSizeFor(path, suggestedSize, context));
        } catch (InputReadFailedException e) {
            throw new ExperimentExecutionException("Cannot establish an input path", e);
        }
    }

    @Override
    public void doJobOnInput(InputBound<StackSequenceInput, MontageSharedState> input)
            throws JobExecutionException {
        Stack stack;
        try {
            stack = input.getInput().asStack(ProgressIgnore.get(), input.getLogger()).projectMax();
        } catch (OperationFailedException e) {
            throw new JobExecutionException(
                    "Cannot extract a stack representation from the input", e);
        }

        try {
            input.getSharedState().copyStackInto(stack, input.getInput().pathForBindingRequired());

        } catch (InputReadFailedException e) {
            throw new JobExecutionException("Cannot establish an input path", e);
        } catch (OperationFailedException e) {
            throw new JobExecutionException("Cannot copy input-stack into combined stack", e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(MontageSharedState sharedState, InputOutputContext context)
            throws ExperimentExecutionException {
        // Write the combined stack
        context.getOutputter()
                .writerSelective()
                .write(
                        OUTPUT_MONTAGE,
                        () -> new StackGenerator(true),
                        sharedState.getStack()::asStack);
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
        return super.defaultOutputs().addEnabledOutputFirst(OUTPUT_MONTAGE);
    }

    /**
     * Determines the scaled size for a particular image, which is used to populate the table. The
     * final size will be approximately similar, but not necessarily identical.
     */
    private Extent scaledSizeFor(
            Path imagePath, Optional<ImageSizeSuggestion> suggestedResize, OperationContext context)
            throws ExperimentExecutionException {
        try {
            ImageMetadata metadata = imageMetadataReader.openFile(imagePath, stackReader, context);
            ScaleFactor factor =
                    scale.calculate(Optional.of(metadata.getDimensions()), suggestedResize);
            return metadata.getDimensions().extent().scaleXYBy(factor);
        } catch (ImageIOException e) {
            throw new ExperimentExecutionException(
                    "Cannot read the image-metadata for file at: " + imagePath, e);
        } catch (OperationFailedException e) {
            throw new ExperimentExecutionException(
                    "Cannot calculate scale for file at: " + imagePath, e);
        }
    }

    /** Creates the {@link StackArranger} that will determine the tabular pattern. */
    private StackArranger createArranger(int numberInputs) {

        // Determine number of columns and rows, so that they are both similar (as close to a square
        // as possible).
        int columns = (int) Math.ceil(Math.sqrt(numberInputs));
        int rows = (int) Math.ceil(((double) numberInputs) / columns);

        // Tiling the images into a tabular form.
        Tile tile = new Tile();
        tile.setNumberColumns(columns);
        tile.setNumberRows(rows);
        tile.setAligner(aligner);
        return tile;
    }

    /** The default {@link ScaleCalculator} if no other is provided. */
    private static ScaleCalculator defaultScaleCalculator() {
        ScaleCalculator fallback =
                new ToDimensions(DEFAULT_WIDTH_SCALE, DEFAULT_HEIGHT_SCALE, true);
        return new ToSuggested(fallback);
    }
}
