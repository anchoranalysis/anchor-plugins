package org.anchoranalysis.plugin.image.task.bean.thumbnail.stack;

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;

/**
 * Creates a thumbnail from a stack
 * 
 * @author Owen Feehan
 *
 */
public abstract class ThumbnailFromStack extends AnchorBean<ThumbnailFromStack> {

    /** Should always be called once before any calls to {@link #thumbnailFor} */
    public abstract void start();
    
    /** Creates a thumbnail for a stack */
    public abstract DisplayStack thumbnailFor(Stack stack) throws CreateException;
}
