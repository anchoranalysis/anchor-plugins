/*-
 * #%L
 * anchor-plugin-mpp-experiment
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
package org.anchoranalysis.plugin.image.task.bean.segment;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.color.RGBColorBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.core.progress.ProgressIgnore;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.io.InitializationContext;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.io.csv.RowLabels;
import org.anchoranalysis.feature.io.results.LabelHeaders;
import org.anchoranalysis.feature.store.NamedFeatureStoreFactory;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.feature.calculator.FeatureTableCalculator;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.inference.bean.segment.instance.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.image.inference.segment.SegmentedObjects;
import org.anchoranalysis.image.inference.segment.SegmentedObjectsAtScale;
import org.anchoranalysis.image.inference.segment.WithConfidence;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.object.output.grayscale.ObjectsMergedAsMaskGenerator;
import org.anchoranalysis.image.io.object.output.hdf5.HDF5ObjectsGenerator;
import org.anchoranalysis.image.io.object.output.rgb.DrawObjectsGenerator;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.image.io.stack.output.generator.StackGenerator;
import org.anchoranalysis.image.io.stack.time.TimeSequence;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.inference.InferenceModel;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.inference.concurrency.ConcurrentModelPool;
import org.anchoranalysis.inference.concurrency.CreateModelFailedException;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.io.output.writer.WriterRouterErrors;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.EachObjectIndependently;
import org.anchoranalysis.plugin.image.task.bean.feature.ExportFeaturesStyle;
import org.anchoranalysis.plugin.image.task.feature.FeatureExporter;
import org.anchoranalysis.plugin.image.task.feature.FeatureExporterContext;
import org.anchoranalysis.plugin.image.task.feature.InitializationWithEnergyStack;
import org.anchoranalysis.plugin.image.task.feature.calculator.CalculateFeaturesForObjects;
import org.anchoranalysis.plugin.image.task.segment.SharedStateSegmentInstance;
import org.anchoranalysis.plugin.image.task.stack.InitializationFactory;

/**
 * Using a model-pool, performs instance segmentation on an image producing zero, one or more
 * objects per image.
 *
 * <p>Various visualizations and export types are supported.
 *
 * <p>The task will output the segmentation results (in HDF5 form and as a mask) for each input,
 * together with visualizations of the outlines.
 *
 * <p>The task also provides aggregated outputs (features, thumbnails) of extracted objects across
 * all inputs.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>input</td><td>no</td><td>The input image for segmentation.</td></tr>
 * <tr><td>objects</td><td>yes</td><td>Segmented object-masks encoded into HDF5.</td></tr>
 * <tr><td>mask</td><td>yes</td><td>A binary-mask image that binary <i>or</i>s each voxel across the segmented object-masks (scaled to match the input image for model inference).</td></tr>
 * <tr><td>outline</td><td>yes</td><td>A RGB image showing the outline of segmented-objects on top of the input image (scaled to match the input image for model inference).</td></tr>
 * <tr><td>mask_full_scale</td><td>no</td><td>Like <i>mask</i> but on the full-scale input image.</td></tr>
 * <tr><td>outline_full_scale</td><td>no</td><td>Like <i>outline</i> but on on the full-scale input image.</td></tr>
 * <tr><td>summary</td><td>yes</td><td>A CSV file showing basic feature of <i>all</i> segmented-objects across <i>all</i> input images.</td></tr>
 * <tr><td>thumbnails</td><td>yes</td><td>A directory of thumbnails showing the outline of <i>all</i> segmented objects on top of an extracted portion of the respective input-image.</td></tr>
 * <tr><td rowspan="3"><i>inherited from {@link Task}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 * @param <T> model-type in pool
 */
public class SegmentInstanceWithModel<T extends InferenceModel>
        extends Task<StackSequenceInput, SharedStateSegmentInstance<T>> {

    private static final EachObjectIndependently COMBINE_OBJECTS = new EachObjectIndependently();

    private static final NamedFeatureStoreFactory STORE_FACTORY =
            NamedFeatureStoreFactory.parametersOnly();

    /** Output-name for the input-image for the segmentation */
    private static final String OUTPUT_INPUT_IMAGE = "input";

    /** Output-name for HDF5 encoded object-masks */
    private static final String OUTPUT_H5 = "objects";

    /** Output-name for object-masks merged together as a mask */
    private static final String OUTPUT_MERGED_AS_MASK = "mask";

    /** Output-name for a colored outline placed around the masks */
    private static final String OUTPUT_OUTLINE = "outline";

    /** Output-name for the feature-results CSV for all objects. */
    private static final String OUTPUT_SUMMARY_CSV = "summary";

    /**
     * A suffix appended to some of the existing output-names, to generate an equivalent output but
     * but on a scaled version of the input image.
     *
     * <p>The scaled version corresponds to the image inputted to the model for inference.
     */
    private static final String OUTPUT_NAME_SCALED_SUFFIX = "_full_scale";

    private static final String MANIFEST_FUNCTION_INPUT_IMAGE = "input_image";

    /** All the outputs that occur <i>per job</i>. */
    private static final String EXECUTION_TIME_OUTPUTS = "All outputs excl. thumbnails";

    private static final String EXECUTION_TIME_SEGMENTATION = "Segmentation";

    private static final String EXECUTION_TIME_FEATURES = "Calculating features + thumbnails";

    private static final String[] FEATURE_LABEL_HEADERS =
            new String[] {"image", "object", "confidence"};

    // START BEAN FIELDS
    /** The segmentation algorithm. */
    @BeanField @Getter @Setter private SegmentStackIntoObjectsPooled<T> segment;

    /** The width of the outline. */
    @BeanField @Getter @Setter private int outlineWidth = 1;

    /** The color of the outline. */
    @BeanField @OptionalBean @Getter @Setter
    private RGBColorBean outlineColor = new RGBColorBean(Color.GREEN);

    /**
     * If true the colors change for different objects in the image (using a default color set).
     *
     * <p>This takes precedence over {@code outlineColor}.
     */
    @BeanField @Getter @Setter private boolean varyColors = false;

    /**
     * Features to calculate for objects in the features output.
     *
     * <p>If unspecified, default features of bounding-box coordinates and number of voxels are
     * selected.
     */
    @BeanField @OptionalBean @Getter @Setter
    private List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>> features;

    /**
     * If true, then the outputs (outline, mask, image etc.) are not written for images that produce
     * no objects.
     */
    @BeanField @Getter @Setter private boolean ignoreNoObjects = true;

    /** Visual style for how feature export occurs. */
    @BeanField @Getter @Setter ExportFeaturesStyle style = new ExportFeaturesStyle();
    // END BEAN FIELDS

    @Override
    public InputTypesExpected inputTypesExpected() {
        // A stack is needed, not individual channels
        return new InputTypesExpected(StackSequenceInput.class);
    }

    @Override
    public SharedStateSegmentInstance<T> beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan plan,
            List<StackSequenceInput> inputs,
            ParametersExperiment parameters)
            throws ExperimentExecutionException {
        try {
            initializeBeans(parameters.createInitializationContext());

            ConcurrentModelPool<T> modelPool =
                    segment.createModelPool(plan, parameters.getContext().getLogger()); // NOSONAR

            LabelHeaders headers = new LabelHeaders(FEATURE_LABEL_HEADERS);
            FeatureExporterContext context = style.deriveContext(parameters.getContext());
            return new SharedStateSegmentInstance<>(
                    modelPool, tableCalculator(), headers, OUTPUT_SUMMARY_CSV, context);

        } catch (CreateModelFailedException | InitializeException | CreateException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public void doJobOnInput(InputBound<StackSequenceInput, SharedStateSegmentInstance<T>> input)
            throws JobExecutionException {
        try {
            initializeBeans(input.createInitializationContext());

            Stack stack = inputStack(input);

            ExecutionTimeRecorder recorder =
                    input.getContextExperiment().getExecutionTimeRecorder();
            SegmentedObjects segments =
                    recorder.recordExecutionTime(
                            EXECUTION_TIME_SEGMENTATION,
                            () ->
                                    segment.segment(
                                            stack,
                                            input.getSharedState().getModelPool(),
                                            input.getContextExperiment()
                                                    .getExecutionTimeRecorder()));

            if (!segments.isEmpty() || !ignoreNoObjects) {
                recorder.recordExecutionTime(
                        EXECUTION_TIME_OUTPUTS,
                        () ->
                                writeOutputsForImage(
                                        stack, segments, input.getContextJob().getOutputter()));
                recorder.recordExecutionTime(
                        EXECUTION_TIME_FEATURES,
                        () ->
                                calculateFeaturesForImage(
                                        input,
                                        stack,
                                        segments.getObjects().atInputScale().listWithoutLabels()));
            }

        } catch (SegmentationFailedException | OperationFailedException | InitializeException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(
            SharedStateSegmentInstance<T> sharedState, InputOutputContext context)
            throws ExperimentExecutionException {
        try {
            sharedState.closeAndWriteOutputs(style);
            closeModelPool(sharedState.getModelPool(), context.getErrorReporter());
        } catch (OperationFailedException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs()
                .addEnabledOutputFirst(
                        OUTPUT_MERGED_AS_MASK,
                        OUTPUT_OUTLINE,
                        FeatureExporter.OUTPUT_THUMBNAILS,
                        OUTPUT_SUMMARY_CSV);
    }

    private void calculateFeaturesForImage(
            InputBound<StackSequenceInput, SharedStateSegmentInstance<T>> input,
            Stack stack,
            List<WithConfidence<ObjectMask>> segments)
            throws OperationFailedException {

        if (segments.isEmpty()) {
            // Exit early, nothing to do, as there are no objects.
            return;
        }

        EnergyStack energyStack = new EnergyStack(stack);

        ObjectCollection objects = deriveObjects(segments);

        CalculateFeaturesForObjects<FeatureInputSingleObject> calculator =
                new CalculateFeaturesForObjects<>(
                        COMBINE_OBJECTS,
                        new InitializationWithEnergyStack(
                                energyStack, input.createInitializationContext()),
                        true,
                        input.getSharedState()
                                .createCalculationContext(
                                        input.getContextJob().getExecutionTimeRecorder(),
                                        input.getContextJob()));

        String imageIdentifier = input.getInput().identifier();

        calculator.calculateForObjects(
                objects,
                energyStack,
                (objectIdentifier, groupGeneratorName, index) ->
                        identifierFor(
                                imageIdentifier,
                                objectIdentifier,
                                segments.get(index).getConfidence()));
    }

    private static RowLabels identifierFor(
            String imageIdentifier, String objectIdentifier, double confidence) {
        return new RowLabels(
                Optional.of(
                        new String[] {
                            imageIdentifier, objectIdentifier, Double.toString(confidence)
                        }),
                Optional.empty());
    }

    private void writeOutputsForImage(
            Stack stack, SegmentedObjects segmentedObjects, Outputter outputter) {

        WriterRouterErrors writer = outputter.writerSelective();

        writer.write(
                OUTPUT_INPUT_IMAGE,
                () -> new StackGenerator(true, Optional.of(MANIFEST_FUNCTION_INPUT_IMAGE), false),
                () -> stack);

        writer.write(
                OUTPUT_H5,
                HDF5ObjectsGenerator::new,
                segmentedObjects.getObjects().atInputScale()::objects);

        assert (segmentedObjects.getObjects().atInputScale().objects().get(0).numberVoxelsOn()
                > segmentedObjects.getObjects().atModelScale().objects().get(0).numberVoxelsOn());

        writeOuputsAtScale(
                writer, segmentedObjects.getObjects().atInputScale(), OUTPUT_NAME_SCALED_SUFFIX);
        writeOuputsAtScale(writer, segmentedObjects.getObjects().atModelScale(), "");
    }

    private void writeOuputsAtScale(
            WriterRouterErrors writer, SegmentedObjectsAtScale memoized, String outputNameSuffix) {
        writer.write(
                OUTPUT_MERGED_AS_MASK + outputNameSuffix,
                () -> new ObjectsMergedAsMaskGenerator(memoized.background().dimensions()),
                memoized::objects);

        writer.write(
                OUTPUT_OUTLINE + outputNameSuffix,
                () -> outlineGenerator(memoized.size(), memoized.backgroundDisplayStack()),
                memoized::objectsWithProperties);
    }

    private DrawObjectsGenerator outlineGenerator(int objectsSize, DisplayStack background) {
        if (varyColors) {
            return DrawObjectsGenerator.outlineVariedColors(objectsSize, outlineWidth, background);
        } else {
            return DrawObjectsGenerator.outlineSingleColor(
                    outlineWidth, background, outlineColor.toRGBColor());
        }
    }

    /**
     * The stack to use as an input to the segmentation algorithm. Always uses the first timepoint.
     */
    private Stack inputStack(InputBound<StackSequenceInput, ?> input)
            throws OperationFailedException {
        return input.getContextJob()
                .getExecutionTimeRecorder()
                .recordExecutionTime(
                        "Reading input stacks",
                        () -> {
                            try {
                                TimeSequence sequence =
                                        input.getInput()
                                                .createStackSequenceForSeries(0, input.getLogger())
                                                .get(ProgressIgnore.get());
                                return sequence.get(0);

                            } catch (ImageIOException e) {
                                throw new OperationFailedException(e);
                            }
                        });
    }

    private void initializeBeans(InitializationContext context) throws InitializeException {
        ImageInitialization initialization = InitializationFactory.createWithoutStacks(context);
        segment.initializeRecursive(initialization, context.getLogger());
    }

    private FeatureTableCalculator<FeatureInputSingleObject> tableCalculator()
            throws CreateException {
        if (features == null) {
            return COMBINE_OBJECTS.createFeatures(FeaturesCreator.defaultInstanceSegmentation());
        } else {
            return COMBINE_OBJECTS.createFeatures(features, STORE_FACTORY, true);
        }
    }

    /** Closes the model-pool, logging any error that may occur. */
    private void closeModelPool(ConcurrentModelPool<T> modelPool, ErrorReporter errorReporter) {
        try {
            modelPool.close();
        } catch (Exception e) {
            errorReporter.recordError(
                    SegmentInstanceWithModel.class,
                    "An error occurred closing the inference model",
                    e);
        }
    }

    private static ObjectCollection deriveObjects(List<WithConfidence<ObjectMask>> segments) {
        return new ObjectCollection(segments.stream().map(WithConfidence::getElement));
    }
}
