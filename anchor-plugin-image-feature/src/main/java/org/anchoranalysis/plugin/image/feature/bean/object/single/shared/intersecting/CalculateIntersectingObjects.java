/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.index.ObjectCollectionRTree;
import org.anchoranalysis.image.object.ObjectCollection;

/**
 * Calculates the intersecting set of objects from a particular collection (represted by an id) and
 * the object-mask in the params
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateIntersectingObjects
        extends FeatureCalculation<ObjectCollection, FeatureInputSingleObject> {

    /**
     * A unique ID that maps 1 to 1 to {@code searchObjects} (and is therefore sufficient to
     * uniquely @{code hashCode()})
     */
    private final String id;

    /** The objects corresponding to id */
    private final ObjectCollection searchObjects;

    @Override
    protected ObjectCollection execute(FeatureInputSingleObject params) {

        ObjectCollectionRTree bboxRTree = new ObjectCollectionRTree(searchObjects);
        return bboxRTree.intersectsWith(params.getObject());
    }
}
