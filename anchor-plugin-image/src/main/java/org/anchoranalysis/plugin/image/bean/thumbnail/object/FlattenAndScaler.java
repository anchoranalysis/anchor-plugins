package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import java.util.Optional;
import org.anchoranalysis.core.cache.LRUCache;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.stack.Stack;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor class FlattenAndScaler {

    private static final int CACHE_SIZE = 1000;
    
    private final ScaleFactor scaleFactor;
    
    /** As (when we are drawing outlines) we can end up scaling objects down multiple times, this caches results for efficiency */ 
    private final LRUCache<ObjectMask, ObjectMask> cacheScaledObjects;
        
    public FlattenAndScaler(ScaleFactor scaleFactor, Interpolator interpolator) {
        this.scaleFactor = scaleFactor;
        this.cacheScaledObjects = new LRUCache<>(CACHE_SIZE, object->object.flattenZ().scale(scaleFactor, interpolator) );
    }    
    
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
    public ObjectMask scaleObject(ObjectMask object) throws OperationFailedException {
        try {
            return cacheScaledObjects.get(object);
        } catch (GetOperationFailedException e) {
            throw new OperationFailedException(e);
        }
    }
    
    /**
     * Flattens and scales an object if it exists
     * 
     * @param object unscaled object
     * @return a scaled object
     * @throws OperationFailedException 
     */
    public ObjectCollection scaleObjects(ObjectCollection objects) throws OperationFailedException {
        return objects.stream().map(this::scaleObject);
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