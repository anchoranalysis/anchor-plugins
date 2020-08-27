package org.anchoranalysis.plugin.mpp.experiment.bean.define;

import java.awt.Color;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.TaskWithoutSharedState;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.segment.object.SegmentStackIntoObjects;
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
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.writer.WriterRouterErrors;
import org.anchoranalysis.plugin.io.bean.input.stack.StackSequenceInput;
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
public class SegmentInstanceTask extends TaskWithoutSharedState<StackSequenceInput> {

    /** Output-name for the input-image for the segmentation */
    private static final String OUTPUT_INPUT_IMAGE = "input";
    
    /** Output-name for HDF5 encoded object-masks */
    private static final String OUTPUT_H5 = "objects";
    
    /** Output-name for object-masks merged together as a mask */
    private static final String OUTPUT_MERGED_AS_MASK = "mask";
    
    /** Output-name for a colored outline placed around the masks */
    private static final String OUTPUT_OUTLINE = "outline";
    
    private static final String MANIFEST_FUNCTION_INPUT_IMAGE = "input_image";
    
    // START BEAN FIELDS
    /** The segmentation algorithm */
    @BeanField @Getter @Setter
    private SegmentStackIntoObjects segment;
    
    /** The width of the outline */
    @BeanField @Getter @Setter
    private int outlineWidth = 1;
    
    /** The color of the outline */
    @OptionalBean @Getter @Setter private RGBColorBean outlineColor = new RGBColorBean(Color.GREEN);
    
    /** 
     * If true the colors change for different objects in the image (using a default color set).
     * 
     * <p>This takes precedence over {@code outlineColor}.
     */
    @Getter @Setter private boolean varyColors = false;
    // END BEAN FIELDS

    @Override
    public InputTypesExpected inputTypesExpected() {
        // A stack is needed, not individual channels
        return new InputTypesExpected(StackSequenceInput.class);
    }
    
    @Override
    public void doJobOnInputObject(InputBound<StackSequenceInput, NoSharedState> input)
            throws JobExecutionException {
        try {
            initializeBeans(input);

            Stack stack = inputStack(input);
            
            ObjectCollection objects = segment.segment(stack);

            DisplayStack background = DisplayStack.create(stack.extractUpToThreeChannels());
            
            writeOutputsForImage( stack, objects, background, input.context().getOutputManager() );
                        
        } catch (SegmentationFailedException | OperationFailedException | InitException | CreateException e) {
            throw new JobExecutionException(e);
        }
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
    private Stack inputStack(InputBound<StackSequenceInput, NoSharedState> input) throws OperationFailedException {
        try {
            TimeSequence sequence = input.getInputObject().createStackSequenceForSeries(0).get(ProgressReporterNull.get());
            return sequence.get(0);
        } catch (RasterIOException e) {
            throw new OperationFailedException(e);
        }
    }
    
    private void initializeBeans(InputBound<StackSequenceInput, NoSharedState> input) throws InitException {
        ImageInitParams params = ImageInitParamsFactory.create(input.context());
        segment.initRecursive(params, input.getLogger());        
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }
}
