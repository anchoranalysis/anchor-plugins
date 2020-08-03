package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;

/**
 * Creates a thumbnail of one or more objects on a stack by drawing the outline of the objects
 *
 * @author Owen Feehan
 */
public abstract class ThumbnailFromObjects extends AnchorBean<ThumbnailFromObjects> {

    /**
     * Initializes the thumbnail creator
     *
     * <p>Should always be called once before any calls to {@link #thumbnailFor}
     *
     * @param objects the entire set of objects for which thumbnails may be subsequently created
     * @param boundingBoxes bounding-boxes that minimally enclose all the inputs to feature rows (e.g. a pair of objects or a single-object) and can be used for guessing scale-factors. A supplier is used as the stream may be desired multiple times.
     * @param background a stack that will be used to form the background (or some part of may be
     *     used)
     */
    public abstract void start(ObjectCollection objects, Supplier<Stream<BoundingBox>> boundingBoxes, Optional<Stack> background)
            throws OperationFailedException;

    /** Creates a thumbnail for one or more objects */
    public abstract DisplayStack thumbnailFor(ObjectCollection objects) throws CreateException;

    /** Performs clean-up (important to clear caches!) */
    public abstract void end();
}
