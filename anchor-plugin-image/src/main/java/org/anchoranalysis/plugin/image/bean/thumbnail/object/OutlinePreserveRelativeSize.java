package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.interpolator.InterpolatorBean;
import org.anchoranalysis.image.bean.interpolator.InterpolatorBeanLanczos;
import org.anchoranalysis.image.bean.size.SizeXY;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.io.generator.raster.bbox.DrawObjectOnStackGenerator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.RGBColorBean;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import lombok.Getter;
import lombok.Setter;

/**
 * Create a thumbnail by drawing an outline of an object at a particular-scale, and placing it centered in a window of a certain size.
 * <p>
 * Preserves the relative-size between objects (i.e. they are all reduced by the same scale-factor) and all aspect-ratios.
 * <p>
 * If it's a z-stack, a maximum intensity projection is first applied.
 * <p>
 * All thumbnails are created with identical size. An error will occur if the background is ever smaller than the thumbnail size.
 * <p>
 * If no specific background-channel is set with {@link backgroundChannelIndex} then the following scheme applies:
 * <ul>
 * <li>If {@code backgroundSource} has exactly zero channels, an empty zero-valued monochrome background is used (unsigned 8 bit).
 * <li>If {@code backgroundSource} has exactly one channel, it's used as a monochrome background
 * <li>If {@code backgroundSource} has exactly three channels, it's used as a RGB background
 * <li>If {@code backgroundSource} has any other number of channels, the first channel is used as a background.
 * </ul>
 * 
 * @author Owen Feehan
 *
 */
public class OutlinePreserveRelativeSize extends ThumbnailFromObjects {

    // START BEAN PROPERTIES
    /** Size of all created thumbnails */
    @BeanField @Getter @Setter private SizeXY size = new SizeXY(200, 200);
    
    /** Uses only this channel (identified by an index in the stack) as the background, -1 disables. */
    @BeanField @Getter @Setter private int backgroundChannelIndex = -1;
    
    /** Interpolator used when scaling */
    @BeanField @Getter @Setter private InterpolatorBean interpolator = new InterpolatorBeanLanczos();
    
    /** The width of the outline. By default, it's 3 as it's nice to have a strongly easily-visible emphasis on where the object is in a thumbnail. */
    @BeanField @Getter @Setter private int outlineWidth = 3;
    
    /** Optionally outline the other (unselected for the thumbnail) objects in this particular color. If not set, these objects aren't outlined at all.. */
    @BeanField @OptionalBean @Getter @Setter private RGBColorBean colorUnselectedObjects;
    // END BEAN PROPERTIES
    
    private DrawObjectOnStackGenerator generator;
    private FlattenAndScaler scaler;
    private Extent sceneExtentScaled;
    
    @Override
    public void start(ObjectCollection objects, Optional<Stack> backgroundSource) throws OperationFailedException {
        
        if (objects.isEmpty()) {
            // Nothing to do, no thumbnails will ever be generated
            return;
        }
        
        // Determine what to scale the objects and any background by
        scaler = new FlattenAndScaler(
                ObjectScalingHelper.scaleEachObjectFitsIn(objects, size.asExtent()),
                interpolator.create());
        
        setupGenerator(objects, determineBackgroundMaybeOutlined(backgroundSource, objects) );
    }
            
    @Override
    public DisplayStack thumbnailFor(ObjectCollection objects) throws CreateException {
        assert(objects.size()==1);
        // For now only work with the first object in the collection
        try {
            ObjectMask objectScaled = scaler.scaleObject(objects.get(0));
            
            // Find a bounding-box of target size in which objectScaled is centered
            BoundingBox centeredBox = CenterBoundingBoxHelper.deriveCenteredBoxWithSize(objectScaled.getBoundingBox(), size.asExtent(), sceneExtentScaled);
            
            assert(centeredBox.extent().equals(size.asExtent()));
            assert(sceneExtentScaled.contains(centeredBox));
            
            ObjectMask objectCentered = objectScaled.mapBoundingBoxChangeExtent(centeredBox);
            
            generator.setIterableElement(objectCentered);
            
            return DisplayStack.create( generator.generate() );
            
        } catch (OutputWriteFailedException | OperationFailedException e) {
            throw new CreateException(e);
        }
    }
    
    /**
     * Sets up the generator and the related {@code sceneExtentScaled} variable
     * 
     * @param objectsUnscaled unscaled objects
     * @param backgroundUnscaled unscaled background if it exists
     */
    private void setupGenerator(ObjectCollection objectsUnscaled, Optional<Stack> backgroundScaled) {
        sceneExtentScaled = scaler.extentFromStackOrObjects(backgroundScaled, objectsUnscaled);
        
        // Create a generator that draws objects on the background
        generator = DrawObjectOnStackGenerator.createFromStack(backgroundScaled, outlineWidth);
    }
    
    private Optional<Stack> determineBackgroundMaybeOutlined(Optional<Stack> backgroundSource, ObjectCollection objects) throws OperationFailedException {
        Optional<Stack> backgroundScaled = BackgroundHelper.determineBackgroundAndScale(backgroundSource, backgroundChannelIndex, scaler);
        
        if (colorUnselectedObjects!=null && backgroundScaled.isPresent()) {
            // Draw the other objects (scaled) onto the background. The objects are memoized as we do again
            // individually at a later point.
            DrawOutlineHelper drawOutlineHelper = new DrawOutlineHelper(colorUnselectedObjects, outlineWidth, scaler); 
            return Optional.of( drawOutlineHelper.drawObjects(backgroundScaled.get(), objects) );
        } else {
            return backgroundScaled;    
        }
    }
    
    @Override
    public void end() {
        // Garbage collect the scaler as it contains a cache of scaled-objects
        scaler = null;
    }
}
