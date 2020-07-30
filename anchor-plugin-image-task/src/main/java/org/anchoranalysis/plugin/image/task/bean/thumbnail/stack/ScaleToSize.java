package org.anchoranalysis.plugin.image.task.bean.thumbnail.stack;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.interpolator.InterpolatorBean;
import org.anchoranalysis.image.bean.interpolator.InterpolatorBeanLanczos;
import org.anchoranalysis.image.bean.size.SizeXY;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import lombok.Getter;
import lombok.Setter;

/**
 * Create a thumbnail by scales an image to a particular size.
 * <p>
 * The aspect ratio between width and height is not preserved.
 * <p>
 * If it's a z-stack, a maximum intensity projection is first applied.
 * 
 * @author Owen Feehan
 *
 */
public class ScaleToSize extends ThumbnailFromStack {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private SizeXY size = new SizeXY(200, 200);
    
    @BeanField @Getter @Setter private InterpolatorBean interpolator = new InterpolatorBeanLanczos();
    // END BEAN PROPERTIES
    
    private Interpolator interpolatorCreated;
    
    @Override
    public void start() {
        interpolatorCreated = interpolator.create(); 
    }
    
    @Override
    public DisplayStack thumbnailFor(Stack stack) throws CreateException {
        
        try {
            Stack resized = stack.mapChannel( channel->
                channel.resizeXY(size.getWidth(), size.getHeight(), interpolatorCreated)
            );
            
            return DisplayStack.create( resized.extractUpToThreeChannels());
            
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
