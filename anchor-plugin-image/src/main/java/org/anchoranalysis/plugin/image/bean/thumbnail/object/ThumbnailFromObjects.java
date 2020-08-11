/*-
 * #%L
 * anchor-plugin-image
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
package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import java.util.Optional;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.StreamableCollection;
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
    public abstract void start(ObjectCollection objects, StreamableCollection<BoundingBox> boundingBoxes, Optional<Stack> background)
            throws OperationFailedException;

    /** Creates a thumbnail for one or more objects */
    public abstract DisplayStack thumbnailFor(ObjectCollection objects) throws CreateException;

    /** Performs clean-up (important to clear caches!) */
    public abstract void end();
}
