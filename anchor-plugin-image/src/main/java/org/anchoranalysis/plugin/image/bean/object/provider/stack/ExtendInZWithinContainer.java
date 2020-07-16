/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.stack;

import java.util.List;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.match.MatcherIntersectionHelper;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithContainer;

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
public class ExtendInZWithinContainer extends ObjectCollectionProviderWithContainer {

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

        BoundingBox bbox = potentialZExpansion(flattened, container);

        // We update these values after our intersection with the container, in case they have
        // changed
        assert (container.getBoundingBox().contains().box(bbox));

        return ExtendObjectsInZHelper.createExtendedObject(
                flattened, container, bbox, (int) object.centerOfGravity().getZ());
    }

    private static BoundingBox potentialZExpansion(ObjectMask objectFlattened, ObjectMask container)
            throws CreateException {

        int zLow = container.getBoundingBox().cornerMin().getZ();
        int zHigh = container.getBoundingBox().calcCornerMax().getZ();

        Extent extent =
                objectFlattened.getBoundingBox().extent().duplicateChangeZ(zHigh - zLow + 1);
        ReadableTuple3i cornerMin =
                objectFlattened.getBoundingBox().cornerMin().duplicateChangeZ(zLow);

        return new BoundingBox(cornerMin, extent)
                .intersection()
                .with(container.getBoundingBox())
                .orElseThrow(() -> new CreateException("Bounding boxes don't intersect"));
    }
}
