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

package org.anchoranalysis.plugin.image.bean.object.filter.independent;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.filter.ObjectFilterPredicate;

/**
 * Only accepts an object if it has greater (or EQUAL) intersection with {@code objectsGreater} than
 * {@code objectsLesser}
 *
 * <p>If an object intersects with neither, it still gets accepted, as both return 0
 *
 * @author Owen Feehan
 */
public class GreaterIntersectionWith extends ObjectFilterPredicate {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objectsGreater;

    @BeanField @Getter @Setter private ObjectCollectionProvider objectsLesser;
    // END BEAN PROPERTIES

    private ObjectCollection intersectionGreater;
    private ObjectCollection intersectionLesser;

    @Override
    protected void start(Optional<Dimensions> dimensions, ObjectCollection objectsToFilter)
            throws OperationFailedException {
        super.start(dimensions, objectsToFilter);
        try {
            intersectionGreater = objectsGreater.get();
            intersectionLesser = objectsLesser.get();

        } catch (ProvisionFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    protected boolean precondition(ObjectCollection objectsToFilter) {
        return true;
    }

    @Override
    protected boolean match(ObjectMask object, Optional<Dimensions> dimensions)
            throws OperationFailedException {

        int countGreater = countIntersectingVoxels(intersectionGreater, object);
        int countLesser = countIntersectingVoxels(intersectionLesser, object);

        return countGreater >= countLesser;
    }

    @Override
    protected void end() throws OperationFailedException {
        intersectionGreater = null;
        intersectionLesser = null;
    }

    /**
     * The count the number of intersecting voxels between all the objects in this collection and
     * another {@code toIntersectWith}.
     *
     * <p>This is the sum of the number of intersecting voxels for each object independently with
     * {@code toIntersectWith}.
     *
     * @param toIntersectWith the object to count the intersection with.
     * @return the total number of intersecting voxels, as per the above definition, or 0 if no
     *     intersection occurs.
     */
    private static int countIntersectingVoxels(
            ObjectCollection objects, ObjectMask toIntersectWith) {

        int count = 0;
        for (ObjectMask other : objects) {
            count += other.countIntersectingVoxels(toIntersectWith);
        }
        return count;
    }
}
