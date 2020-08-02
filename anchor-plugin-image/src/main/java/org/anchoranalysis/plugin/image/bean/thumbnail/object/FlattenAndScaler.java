package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import java.util.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.stack.Stack;
import lombok.AllArgsConstructor;

@AllArgsConstructor class FlattenAndScaler {

    private final ScaleFactor scaleFactor;
    private Interpolator interpolator;
        
    /**
     * Flattens and scales a stack if it exists
     * <p>
     * Any resolution information is also removed.
     * 
     * @param stack stack to flatten and scale
     * @return the flattened and scaled stack
     */
    public Optional<Stack> scaleStack(Optional<Stack> stack) {
        try {
            return OptionalUtilities.map(stack, this::flattenScaleAndRemoveResolution);
        } catch (OperationFailedException e) {
            // In this context, this exception should only occur if different sized channels are produced for a stack,
            //  which cannot happen
            throw new AnchorImpossibleSituationException();
        }
    }
    
    /**
     * Reads the extent from a stack that has already been scaled if it exists, or derives an extent from an object collection 
     * @param stackScaled a stack that has already been scaled (and flattened)
     * @param objectsUnscaled objects that have yet to be scaled (and flattened)
     * @return an extent that has been flattened and scaled
     */
    public Extent extentFromStackOrObjects(Optional<Stack> stackScaled, ObjectCollection objectsUnscaled) {
        return stackScaled.map( stack->stack.getDimensions().getExtent() ).orElseGet( ()->deriveScaledExtentFromObjects(objectsUnscaled) );
    }
    
    /**
     * Flattens and scales an object if it exists
     * 
     * @param object unscaled object
     * @return a scaled object
     */
    public ObjectMask scaleObject(ObjectMask object) {
        return object.flattenZ().scale(scaleFactor, interpolator);
    }
    
    
    /** Scales each channel in the stack by the scale-factor and removes any resolution information (which is no longer physically valid) */
    private Stack flattenScaleAndRemoveResolution(Stack stack) throws OperationFailedException {
        return stack.mapChannel(this::flattenScaleAndRemoveResolution);
    }
    
    /** Scales the channel by the scale-factor and removes any resolution information (which is no longer physically valid) */
    private Channel flattenScaleAndRemoveResolution(Channel channel) {
        Channel scaled = channel.maxIntensityProjection().scaleXY(scaleFactor);
        scaled.updateResolution( new ImageResolution() );
        return scaled;
    }
    
    /** Derives what a scaled version of an extent would look like that fits all objects */
    private Extent deriveScaledExtentFromObjects(ObjectCollection objects) {
        return ExtentToFitBoundingBoxes.derive(
             objects.streamStandardJava().map( object->
                object.getBoundingBox().scale(scaleFactor).flattenZ()
        ));
    }
}