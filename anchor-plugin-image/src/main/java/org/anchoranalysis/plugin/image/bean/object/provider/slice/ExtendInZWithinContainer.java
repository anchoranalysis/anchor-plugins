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

package org.anchoranalysis.plugin.image.bean.object.provider.slice;

import java.util.List;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.box.BoundingBox;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.match.MatcherIntersectionHelper;
import org.anchoranalysis.plugin.image.bean.object.provider.WithContainerBase;

/**
 * Extends an object (2D/3D) as much as it can within the z-slices of a containing object
 *
 * <p>An object-collection is produced with an identical number of objects, but with each expanded
 * in the z-dimension.
 *
 * <p>If the input-object is a 2D-slice it is replicated directly, if it is already a 3D-object its
 * flattened version (a maximum intensity projection) is used.
 *
 * @author Owen Feehan
 */
public class ExtendInZWithinContainer extends WithContainerBase {

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objectsSource)
            throws CreateException {

        List<MatchedObject> matchList =
                MatcherIntersectionHelper.matchIntersectingObjects(
                        containerRequired(),
                        objectsSource.duplicate() // Duplicated so as to avoid changing the original
                        );

        // For each obj we extend it into its container
        ObjectCollection out = new ObjectCollection();

        for (MatchedObject owm : matchList) {
            for (ObjectMask other : owm.getMatches()) {

                out.add(createExtendedObject(other, owm.getSource()));
            }
        }

        return out;
    }

    private static ObjectMask createExtendedObject(ObjectMask object, ObjectMask container)
            throws CreateException {

        ObjectMask flattened = object.flattenZ();

        BoundingBox box = potentialZExpansion(flattened, container);

        // We update these values after our intersection with the container, in case they have
        // changed
        assert (container.boundingBox().contains().box(box));

        return ExtendObjectsInZHelper.createExtendedObject(
                flattened, container, box, (int) object.centerOfGravity().z());
    }

    private static BoundingBox potentialZExpansion(ObjectMask objectFlattened, ObjectMask container)
            throws CreateException {

        int zLow = container.boundingBox().cornerMin().z();
        int zHigh = container.boundingBox().calculateCornerMax().z();

        Extent extent = objectFlattened.boundingBox().extent().duplicateChangeZ(zHigh - zLow + 1);
        ReadableTuple3i cornerMin =
                objectFlattened.boundingBox().cornerMin().duplicateChangeZ(zLow);

        return new BoundingBox(cornerMin, extent)
                .intersection()
                .with(container.boundingBox())
                .orElseThrow(() -> new CreateException("Bounding boxes don't intersect"));
    }
}
