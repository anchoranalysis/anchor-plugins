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
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.io.csv.RowLabels;
import org.anchoranalysis.feature.io.results.LabelHeaders;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.session.FeatureTableCalculator;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.generator.raster.StackGenerator;
import org.anchoranalysis.image.io.generator.raster.object.collection.ObjectsMergedAsMaskGenerator;
import org.anchoranalysis.image.io.generator.raster.object.rgb.DrawObjectsGenerator;
import org.anchoranalysis.image.io.input.ImageInitParamsFactory;
import org.anchoranalysis.image.io.objects.GeneratorHDF5;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.TimeSequence;
import org.anchoranalysis.io.bean.color.RGBColorBean;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.writer.WriterRouterErrors;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.WithConfidence;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.EachObjectIndependently;
import org.anchoranalysis.plugin.image.task.feature.CalculateFeaturesForObjects;
import org.anchoranalysis.plugin.image.task.feature.InitParamsWithEnergyStack;
import org.anchoranalysis.plugin.image.task.segment.SharedStateSegmentInstance;
import org.anchoranalysis.plugin.io.bean.input.stack.StackSequenceInput;

/**
 * Using a model-pool, performs instance segmentation on an image producing zero, one or more
 * objects per image.
 *
 * <p>Various visualizations and export types are supported.
 *
 * <p>The task will output the segmentation results (in HDF5 form and as a mask) for each input,
 * together with visualizations of the outlines.
 *
 * <p>The task also provides a aggregated outputs (features, thumbnails) of extracted objects across
 * all inputs.
 *
 * @author Owen Feehan
 * @param <T> model-type in pool
 */
public class SegmentInstanceWithModelTask<T>
        extends Task<StackSequenceInput, SharedStateSegmentInstance<T>> {

    private static final EachObjectIndependently COMBINE_OBJECTS = new EachObjectIndependently();

    private static final NamedFeatureStoreFactory STORE_FACTORY =
            NamedFeatureStoreFactory.factoryParamsOnly();

    /** Output-name for the input-image for the segmentation */
    private static final String OUTPUT_INPUT_IMAGE = "input";

    /** Output-name for HDF5 encoded object-masks */
    private static final String OUTPUT_H5 = "objects";

    /** Output-name for object-masks merged together as a mask */
    private static final String OUTPUT_MERGED_AS_MASK = "mask";

    /** Output-name for a colored outline placed around the masks */
    private static final String OUTPUT_OUTLINE = "outline";

    private static final String MANIFEST_FUNCTION_INPUT_IMAGE = "input_image";

    private static final String[] FEATURE_LABEL_HEADERS = new String[] {"image", "object", "confidence"};

    // START BEAN FIELDS
    /** The segmentation algorithm */
    @BeanField @Getter @Setter private SegmentStackIntoObjectsPooled<T> segment;

    /** The width of the outline */
    @BeanField @Getter @Setter private int outlineWidth = 1;

    /** The color of the outline */
    @BeanField @OptionalBean @Getter @Setter
    private RGBColorBean outlineColor = new RGBColorBean(Color.GREEN);

    /**
     * If true the colors change for different objects in the image (using a default color set).
     *
     * <p>This takes precedence over {@code outlineColor}.
     */
    @BeanField @Getter @Setter private boolean varyColors = false;

    /**
     * Features to calculate for objects in the features output. If unspecified, default features of
     * bounding-box coordinates and number of voxels are selected.
     */
    @BeanField @OptionalBean @Getter @Setter
    private List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>> features;

    /**
     * If true, then the outputs (outline, mask, image etc.) are not written for images that produce
     * no objects.
     */
    @BeanField @Getter @Setter private boolean ignoreNoObjects = true;
    // END BEAN FIELDS

    @Override
    public InputTypesExpected inputTypesExpected() {
        // A stack is needed, not individual channels
        return new InputTypesExpected(StackSequenceInput.class);
    }

    @Override
    public SharedStateSegmentInstance<T> beforeAnyJobIsExecuted(
            BoundOutputManagerRouteErrors outputManager,
            ConcurrencyPlan plan,
            ParametersExperiment params)
            throws ExperimentExecutionException {
        try {
            initializeBeans(params.getContext());
            ConcurrentModelPool<T> modelPool = segment.createModelPool(plan);

            LabelHeaders headers = new LabelHeaders(FEATURE_LABEL_HEADERS);
            return new SharedStateSegmentInstance<>(
                    modelPool, tableCalculator(), headers, params.getContext());
        } catch (CreateException | InitException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public void doJobOnInputObject(
            InputBound<StackSequenceInput, SharedStateSegmentInstance<T>> input)
            throws JobExecutionException {
        try {
            initializeBeans(input.context());

            Stack stack = inputStack(input);

            SegmentedObjects segments =
                    segment.segment(stack, input.getSharedState().getModelPool());

            DisplayStack background = DisplayStack.create(stack.extractUpToThreeChannels());

            if (!segments.isEmpty() || !ignoreNoObjects) {
                writeOutputsForImage(
                        stack, segments.asObjects(), background, input.context().getOutputManager());

                calculateFeaturesForImage(input, stack, segments.asList());
            }

        } catch (SegmentationFailedException
                | OperationFailedException
                | CreateException
                | InitException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(
            SharedStateSegmentInstance<T> sharedState, BoundIOContext context)
            throws ExperimentExecutionException {
        try {
            sharedState.closeAnyOpenIO();
        } catch (IOException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    private void calculateFeaturesForImage(
            InputBound<StackSequenceInput, SharedStateSegmentInstance<T>> input,
            Stack stack,
            List<WithConfidence<ObjectMask>> segments)
            throws OperationFailedException {

        if (segments.isEmpty()) {
            // Exit early, nothing to do
            return;
        }

        EnergyStack energyStack = new EnergyStack(stack);

        CalculateFeaturesForObjects<FeatureInputSingleObject> calculator =
                new CalculateFeaturesForObjects<>(
                        COMBINE_OBJECTS,
                        new InitParamsWithEnergyStack(energyStack, input.context()),
                        true,
                        input.getSharedState()
                                .createInputProcessContext(Optional.empty(), input.context()));
        calculator.calculateFeaturesForObjects(
                deriveObjects(segments),
                energyStack,
                (featureInput, index) ->
                        identifierFor(
                                input.getInputObject().descriptiveName(),
                                featureInput,
                                calculator, segments.get(index).getConfidence()));
    }

    private RowLabels identifierFor(
            String imageIdentifier,
            FeatureInputSingleObject featureInput,
            CalculateFeaturesForObjects<FeatureInputSingleObject> calculator, double confidence) {
        return new RowLabels(
                Optional.of(
                        new String[] {
                            imageIdentifier, calculator.uniqueIdentifierFor(featureInput), Double.toString(confidence)
                        }),
                Optional.empty());
    }

    private void writeOutputsForImage(
            Stack stack,
            ObjectCollection objects,
            DisplayStack background,
            BoundOutputManagerRouteErrors outputManager) {

        WriterRouterErrors writer = outputManager.getWriterCheckIfAllowed();

        writer.write(
                OUTPUT_INPUT_IMAGE,
                () -> new StackGenerator(stack, true, MANIFEST_FUNCTION_INPUT_IMAGE));
        writer.write(OUTPUT_H5, () -> new GeneratorHDF5(objects));
        writer.write(
                OUTPUT_MERGED_AS_MASK,
                () -> new ObjectsMergedAsMaskGenerator(stack.dimensions(), objects));

        writer.write(OUTPUT_OUTLINE, () -> outlineGenerator(objects, background));
    }

    private DrawObjectsGenerator outlineGenerator(
            ObjectCollection objects, DisplayStack background) {
        if (varyColors) {
            return DrawObjectsGenerator.outlineVariedColors(objects, outlineWidth, background);
        } else {
            return DrawObjectsGenerator.outlineSingleColor(
                    objects, outlineWidth, background, outlineColor.rgbColor());
        }
    }

    /**
     * The stack to use as an input to the segmentation algorithm. Always uses the first timepoint.
     */
    private Stack inputStack(InputBound<StackSequenceInput, ?> input)
            throws OperationFailedException {
        try {
            TimeSequence sequence =
                    input.getInputObject()
                            .createStackSequenceForSeries(0)
                            .get(ProgressReporterNull.get());
            return sequence.get(0);
        } catch (RasterIOException e) {
            throw new OperationFailedException(e);
        }
    }

    private void initializeBeans(BoundIOContext context) throws InitException {
        ImageInitParams params = ImageInitParamsFactory.create(context);
        segment.initRecursive(params, context.getLogger());
    }

    private FeatureTableCalculator<FeatureInputSingleObject> tableCalculator()
            throws CreateException {
        if (features == null) {
            return COMBINE_OBJECTS.createFeatures(FeaturesCreator.defaultInstanceSegmentation());
        } else {
            return COMBINE_OBJECTS.createFeatures(features, STORE_FACTORY, true);
        }
    }    
    
    private static ObjectCollection deriveObjects(List<WithConfidence<ObjectMask>> segments) {
        return new ObjectCollection( segments.stream().map(WithConfidence::getObject) );
    }
}
