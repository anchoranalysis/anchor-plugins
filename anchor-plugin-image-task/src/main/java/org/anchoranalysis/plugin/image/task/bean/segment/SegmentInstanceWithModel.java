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
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.color.RGBColorBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.io.InitializationContext;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.io.csv.metadata.LabelHeaders;
import org.anchoranalysis.image.bean.interpolator.Interpolator;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.nonbean.segment.SegmentationFailedException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.feature.calculator.FeatureTableCalculator;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.inference.bean.segment.instance.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.image.inference.segment.SegmentedObjects;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.image.io.stack.time.TimeSeries;
import org.anchoranalysis.inference.InferenceModel;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.inference.concurrency.ConcurrentModelPool;
import org.anchoranalysis.inference.concurrency.CreateModelFailedException;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.EachObjectIndependently;
import org.anchoranalysis.plugin.image.task.bean.feature.ExportFeaturesStyle;
import org.anchoranalysis.plugin.image.task.feature.FeatureExporter;
import org.anchoranalysis.plugin.image.task.feature.FeatureExporterContext;
import org.anchoranalysis.plugin.image.task.segment.SharedStateSegmentInstance;
import org.anchoranalysis.plugin.image.task.stack.InitializationFactory;

/**
 * Using a model-pool, performs instance segmentation on an image producing zero, one or more
 * objects per image.
 *
 * <p>Various visualizations and export types are supported.
 *
 * <ul>
 *   <li>Segmentation results in vairous forms (HDF5) for input into other scripts. (in HDF5 form
 *       and as a mask).
 *   <li>Visualizations of the instances found (outlines, thumbnails etc.)
 *   <li>A table of basic features (CSV) for each instance.
 * </ul>
 *
 * <p>Specifically, the following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>{@value WriteSegmentationOutputsHelper#OUTPUT_INPUT_IMAGE}</td><td>no</td><td>The input image for segmentation.</td></tr>
 * <tr><td>{@value WriteSegmentationOutputsHelper#OUTPUT_H5}</td><td>yes</td><td>Segmented object-masks encoded into HDF5.</td></tr>
 * <tr><td>{@value WriteSegmentationOutputsHelper#OUTPUT_MERGED_AS_MASK}</td><td>yes</td><td>A binary-mask image that binary <i>or</i>s each voxel across the segmented object-masks (scaled to match the input image for model inference).</td></tr>
 * <tr><td>{@value WriteSegmentationOutputsHelper#OUTPUT_OUTLINE}</td><td>yes</td><td>A RGB image showing the outline of segmented-objects on top of the input image (scaled to match the input image for model inference).</td></tr>
 * <tr><td>{@value WriteSegmentationOutputsHelper#OUTPUT_MERGED_AS_MASK}{@value WriteSegmentationOutputsHelper#OUTPUT_NAME_SCALED_SUFFIX}</td><td>no</td><td>Like <i>mask</i> but on the full-scale input image.</td></tr>
 * <tr><td>{@value WriteSegmentationOutputsHelper#OUTPUT_OUTLINE}{@value WriteSegmentationOutputsHelper#OUTPUT_NAME_SCALED_SUFFIX}</td><td>no</td><td>Like <i>outline</i> but on on the full-scale input image.</td></tr>
 * <tr><td>{@value SegmentInstanceWithModel#OUTPUT_SUMMARY_CSV}</td><td>yes</td><td>A CSV file showing basic feature of <i>all</i> segmented-objects across <i>all</i> input images.</td></tr>
 * <tr><td>{@value FeatureExporter#OUTPUT_THUMBNAILS}</td><td>yes</td><td>A directory of thumbnails showing the outline of <i>all</i> segmented objects on top of an extracted portion of the respective input-image.</td></tr>
 * <tr><td rowspan="3"><i>outputs inherited from {@link Task}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 * @param <T> model-type in pool
 */
public class SegmentInstanceWithModel<T extends InferenceModel>
        extends Task<StackSequenceInput, SharedStateSegmentInstance<T>> {

    /** Output-name for the feature-results CSV for all objects. */
    private static final String OUTPUT_SUMMARY_CSV = "summary";

    /** All the outputs that occur <i>per job</i>. */
    private static final String EXECUTION_TIME_OUTPUTS = "All outputs apart from thumbnails";

    private static final String EXECUTION_TIME_SEGMENTATION = "Segmentation";

    private static final String EXECUTION_TIME_FEATURES = "Calculating features + thumbnails";

    private static final String[] FEATURE_LABEL_HEADERS =
            new String[] {"image", "object", "confidence"};

    // START BEAN FIELDS
    /** The segmentation algorithm. */
    @BeanField @Getter @Setter private SegmentStackIntoObjectsPooled<T> segment;

    /** The color of the outline. */
    @BeanField @OptionalBean @Getter @Setter
    private RGBColorBean outlineColor = new RGBColorBean(Color.GREEN);

    /**
     * When true, the colors change for different objects in the image (using a default color set).
     *
     * <p>This takes precedence over {@code outlineColor}.
     */
    @BeanField @Getter @Setter private boolean varyColors = false;

    /** The width of the outline. */
    @BeanField @Getter @Setter private int outlineWidth = 1;

    /**
     * Features to calculate for objects in the features output.
     *
     * <p>If unspecified, default features of bounding-box coordinates and number of voxels are
     * selected.
     */
    @BeanField @OptionalBean @Getter @Setter
    private List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>> features;

    /**
     * When true, then the outputs (outline, mask, image etc.) are not written for images that
     * produce no objects.
     */
    @BeanField @Getter @Setter private boolean ignoreNoObjects = false;

    /** Visual style for how feature export occurs. */
    @BeanField @Getter @Setter ExportFeaturesStyle style = new ExportFeaturesStyle();

    /** The interpolator to use for scaling images. */
    @BeanField @Getter @Setter @DefaultInstance private Interpolator interpolator;
    // END BEAN FIELDS

    /**
     * How to combine objects to form features.
     *
     * <p>This is deliberately lazily created to allow the interpolator to be passed in the
     * constructor.
     */
    private EachObjectIndependently combineObjects;

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
            initializeBeans(parameters.deriveInitializationContext());

            ConcurrentModelPool<T> modelPool =
                    segment.createModelPool(plan, parameters.getContext().getLogger()); // NOSONAR

            LabelHeaders headers = new LabelHeaders(FEATURE_LABEL_HEADERS);
            FeatureExporterContext context = style.deriveContext(parameters.getContext());

            FeatureTableCalculator<FeatureInputSingleObject> tableCalculator =
                    FeatureTableCreator.tableCalculator(
                            Optional.ofNullable(features), combineObjects());
            return new SharedStateSegmentInstance<>(
                    modelPool, tableCalculator, headers, OUTPUT_SUMMARY_CSV, context);

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
                                new WriteSegmentationOutputsHelper(
                                                varyColors, outlineWidth, outlineColor)
                                        .writeOutputsForImage(
                                                stack,
                                                segments,
                                                input.getContextJob().getOutputter()));
                recorder.recordExecutionTime(
                        EXECUTION_TIME_FEATURES,
                        () ->
                                FeatureCalculatorHelper.calculateFeaturesAndThumbnails(
                                        input,
                                        stack,
                                        segments.getObjects().atInputScale().listWithoutLabels(),
                                        combineObjects()));
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
        } catch (OutputWriteFailedException e) {
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
                        WriteSegmentationOutputsHelper.OUTPUT_MERGED_AS_MASK,
                        WriteSegmentationOutputsHelper.OUTPUT_OUTLINE,
                        FeatureExporter.OUTPUT_THUMBNAILS,
                        OUTPUT_SUMMARY_CSV);
    }

    private void initializeBeans(InitializationContext context) throws InitializeException {
        ImageInitialization initialization = InitializationFactory.createWithoutStacks(context);
        segment.initializeRecursive(initialization, context.getLogger());
    }

    /** How to combine objects to form features. */
    private EachObjectIndependently combineObjects() {
        if (combineObjects == null) {
            combineObjects = new EachObjectIndependently(interpolator);
        }
        return combineObjects;
    }

    /**
     * The stack to use as an input to the segmentation algorithm. Always uses the first timepoint.
     */
    private static Stack inputStack(InputBound<StackSequenceInput, ?> input)
            throws OperationFailedException {
        return input.getContextJob()
                .getExecutionTimeRecorder()
                .recordExecutionTime(
                        "Reading input stacks",
                        () -> {
                            try {
                                TimeSeries series =
                                        input.getInput()
                                                .createStackSequenceForSeries(0, input.getLogger());
                                return series.getFrame(0);

                            } catch (ImageIOException e) {
                                throw new OperationFailedException(e);
                            }
                        });
    }

    /** Closes the model-pool, logging any error that may occur. */
    private static void closeModelPool(
            ConcurrentModelPool<?> modelPool, ErrorReporter errorReporter) {
        try {
            modelPool.close();
        } catch (Exception e) {
            errorReporter.recordError(
                    SegmentInstanceWithModel.class,
                    "An error occurred closing the inference model",
                    e);
        }
    }
}
