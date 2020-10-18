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

package org.anchoranalysis.plugin.image.feature.bean.object.combine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.point.Point3i;

/**
 * Generates unique identifiers for object-masks
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class UniqueIdentifierUtilities {

    /**
     * Generates a unique identifier (unique within a particular collection) for an object based
     * upon an assumption that there are no overlapping objects in this collection.
     *
     * @param obj object to generate identifier for
     * @return a string encoded with an arbitrary point that lies on the object, or "none" if the
     *     object has no points
     */
    public static String forObject(ObjectMask obj) {
        return obj.findArbitraryOnVoxel().map(UniqueIdentifierUtilities::forPoint).orElse("none");
    }

    /**
     * Generates a unique identifier (unique within a particular collection) for a pair of objects
     * based upon an assumption that there are no overlapping objects in this collection.
     *
     * @param obj1 first object
     * @param obj2 second object
     * @return a string that combines a unique identifier for the first object and one for the
     *     second object
     */
    public static String forObjectPair(ObjectMask obj1, ObjectMask obj2) {
        StringBuilder sb = new StringBuilder();
        sb.append(forObject(obj1));
        sb.append("_and_");
        sb.append(forObject(obj2));
        return sb.toString();
    }

    private static String forPoint(Point3i point) {
        return String.format("%d_%d_%d", point.x(), point.y(), point.z());
    }
}
