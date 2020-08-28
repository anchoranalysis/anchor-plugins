package org.anchoranalysis.plugin.mpp.experiment.bean.define;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
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
import org.anchoranalysis.feature.io.csv.LabelHeaders;
import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.segment.object.SegmentStackIntoObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.session.FeatureTableCalculator;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.generator.raster.StackGenerator;
import org.anchoranalysis.image.io.generator.raster.object.collection.ObjectsMergedAsMaskGenerator;
import org.anchoranalysis.image.io.generator.raster.object.rgb.DrawObjectsGenerator;
import org.anchoranalysis.image.io.input.ImageInitParamsFactory;
import org.anchoranalysis.image.io.objects.GeneratorHDF5;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.TimeSequence;
import org.anchoranalysis.io.bean.color.RGBColorBean;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.writer.WriterRouterErrors;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.EachObjectIndependently;
import org.anchoranalysis.plugin.image.task.feature.SharedStateExportFeatures;
import org.anchoranalysis.plugin.io.bean.input.stack.StackSequenceInput;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.source.CalculateFeaturesForObjects;
import org.anchoranalysis.plugin.mpp.experiment.feature.source.InitParamsWithEnergyStack;
import lombok.Getter;
import lombok.Setter;

/**
 * Performs instance segmentation on an image producing zero, one or more objects per image.
 * 
 * <p>Various visualizations and export types are supported.
 * 
 * <p>The task will output the segmentation results (in HDF5 form and as a mask) for each input, together with visualizations of the outlines.
 * 
 * <p>The task also provides a aggregated outputs (features, thumbnails) of extracted objects
 * across all inputs. 
 * 
 * @author Owen Feehan
 *
 */
public class SegmentInstanceTask extends Task<StackSequenceInput,SharedStateExportFeatures<FeatureTableCalculator<FeatureInputSingleObject>>> {

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
    
    private static final String[] FEATURE_LABEL_HEADERS = new String[]{"image", "object"};
    
    // START BEAN FIELDS
    /** The segmentation algorithm */
    @BeanField @Getter @Setter
    private SegmentStackIntoObjects segment;
    
    /** The width of the outline */
    @BeanField @Getter @Setter
    private int outlineWidth = 1;
    
    /** The color of the outline */
    @BeanField @OptionalBean @Getter @Setter private RGBColorBean outlineColor = new RGBColorBean(Color.GREEN);
    
    /** 
     * If true the colors change for different objects in the image (using a default color set).
     * 
     * <p>This takes precedence over {@code outlineColor}.
     */
    @BeanField @Getter @Setter private boolean varyColors = false;
    
    /**
     * Features to calculate for objects in the features output. If unspecified, default features of bounding-box coordinates and number of voxels are selected.
     */
    @BeanField @OptionalBean @Getter @Setter private List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>> features;
    
    /**
     * If true, then the outputs (outline, mask, image etc.) are not written for images that produce no objects. 
     */
    @BeanField @Getter @Setter private boolean ignoreNoObjects = true;
    // END BEAN FIELDS

    @Override
    public InputTypesExpected inputTypesExpected() {
        // A stack is needed, not individual channels
        return new InputTypesExpected(StackSequenceInput.class);
    }

    @Override
    public SharedStateExportFeatures<FeatureTableCalculator<FeatureInputSingleObject>> beforeAnyJobIsExecuted(
            BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
            throws ExperimentExecutionException {
        try {
            LabelHeaders headers = new LabelHeaders(FEATURE_LABEL_HEADERS);
            return SharedStateExportFeatures.createForFeatures(tableCalculator(), headers, params.getContext());
        } catch (CreateException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public void doJobOnInputObject(
            InputBound<StackSequenceInput, SharedStateExportFeatures<FeatureTableCalculator<FeatureInputSingleObject>>> input)
            throws JobExecutionException {
        try {
            initializeBeans(input);

            Stack stack = inputStack(input);
            
            ObjectCollection objects = segment.segment(stack);

            DisplayStack background = DisplayStack.create(stack.extractUpToThreeChannels());
            
            if (objects.size() > 0 || !ignoreNoObjects) {
                writeOutputsForImage( stack, objects, background, input.context().getOutputManager() );
    
                calculateFeaturesForImage(input, stack, objects);
            }
                        
        } catch (SegmentationFailedException | OperationFailedException | InitException | CreateException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(
            SharedStateExportFeatures<FeatureTableCalculator<FeatureInputSingleObject>> sharedState, BoundIOContext context)
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
    
    private void calculateFeaturesForImage(InputBound<StackSequenceInput, SharedStateExportFeatures<FeatureTableCalculator<FeatureInputSingleObject>>> input, Stack stack, ObjectCollection objects) throws OperationFailedException {
        
        EnergyStack energyStack = new EnergyStack(stack);
 
        CalculateFeaturesForObjects<FeatureInputSingleObject> calculator = new CalculateFeaturesForObjects<>(
            COMBINE_OBJECTS,
            new InitParamsWithEnergyStack(energyStack, input.context()),
            true,
            input.getSharedState().createInputProcessContext(Optional.empty(), input.context())
        );
        calculator.calculateFeaturesForObjects(objects, energyStack, featureInput -> identifierFor(input.getInputObject().descriptiveName(), featureInput, calculator) );        
    }
    
    private StringLabelsForCsvRow identifierFor(
            String imageIdentifier,
            FeatureInputSingleObject featureInput,
            CalculateFeaturesForObjects<FeatureInputSingleObject> calculator
    ) {
        return new StringLabelsForCsvRow(
                Optional.of(new String[] {imageIdentifier, calculator.uniqueIdentifierFor(featureInput) }),
                Optional.empty());
    }
    
    private void writeOutputsForImage(Stack stack, ObjectCollection objects, DisplayStack background, BoundOutputManagerRouteErrors outputManager) {

        WriterRouterErrors writer = outputManager.getWriterCheckIfAllowed();
        
        writer.write(OUTPUT_INPUT_IMAGE, () -> new StackGenerator(stack, true, MANIFEST_FUNCTION_INPUT_IMAGE) );
        writer.write(OUTPUT_H5, () -> new GeneratorHDF5(objects) );
        writer.write(OUTPUT_MERGED_AS_MASK, () -> new ObjectsMergedAsMaskGenerator(stack.dimensions(), objects) );
        
        writer.write(OUTPUT_OUTLINE, () -> outlineGenerator(objects, background) );
    }
    
    private DrawObjectsGenerator outlineGenerator(ObjectCollection objects, DisplayStack background) {
        if (varyColors) {
            return DrawObjectsGenerator.outlineVariedColors(objects, outlineWidth, background);
        } else {
            return DrawObjectsGenerator.outlineSingleColor(objects, outlineWidth, background, outlineColor.rgbColor());
        }
    }
    
    /** The stack to use as an input to the segmentation algorithm. Always uses the first timepoint. */
    private Stack inputStack(InputBound<StackSequenceInput, ?> input) throws OperationFailedException {
        try {
            TimeSequence sequence = input.getInputObject().createStackSequenceForSeries(0).get(ProgressReporterNull.get());
            return sequence.get(0);
        } catch (RasterIOException e) {
            throw new OperationFailedException(e);
        }
    }
    
    private void initializeBeans(InputBound<?, ?> input) throws InitException {
        ImageInitParams params = ImageInitParamsFactory.create(input.context());
        segment.initRecursive(params, input.getLogger());        
    }
        
    private FeatureTableCalculator<FeatureInputSingleObject> tableCalculator() throws CreateException {
        if (features==null) {
            return COMBINE_OBJECTS.createFeatures(FeaturesCreator.defaultInstanceSegmentation());
        } else {
            return COMBINE_OBJECTS.createFeatures(features, STORE_FACTORY, true);
        }
    }
}
