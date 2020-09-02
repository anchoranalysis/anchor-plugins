/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.object.pair.touching;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.extent.box.BoundingBox;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Calculates objects/bounding boxes relative to others
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class RelativeUtilities {

    /**
     * Changes (immutably) the coordinates of a bounding-box to make them relative to another object
     *
     * @param box a bounding-box
     * @param objectRelativeBase object to make the bounding-box relative to
     * @return a new bounding box with relative coordinates
     */
    public static BoundingBox createRelBBox(BoundingBox box, ObjectMask objectRelativeBase) {
        BoundingBox boxIntersectRel =
                new BoundingBox(
                        box.relativePositionTo(objectRelativeBase.boundingBox()), box.extent());
        return boxIntersectRel.clipTo(objectRelativeBase.boundingBox().extent());
    }

    /**
     * Changes the coordinates of an object to make them relative to another object
     *
     * @param object the object
     * @param objectRelativeBase the other object to use as a base to make om relative to
     * @return a new object with new bounding-box (but with identical memory used for the mask)
     */
    public static ObjectMask createRelMask(ObjectMask object, ObjectMask objectRelativeBase) {
        return object.relativeMaskTo(objectRelativeBase.boundingBox())
                .mapBoundingBoxPreserveExtent(BoundingBox::reflectThroughOrigin);
    }
}
