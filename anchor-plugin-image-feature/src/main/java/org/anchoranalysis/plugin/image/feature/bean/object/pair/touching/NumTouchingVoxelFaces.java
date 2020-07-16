/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.pair.touching;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;

/**
 * A scheme for counting the touching voxels by intersection of object-masks
 *
 * <p>Specifically, one of the object-masks is dilated, and count the number of intersecting pixels
 * with another mask.
 *
 * <p>However, intersection(a*,b)!=intersection(a,b*) where * is the dilation operator. Different
 * counts occur as a single-voxel can have multiple edges with the neighbor.
 *
 * <p>So it's better if we can iterate with a kernel over the edge pixels, and count the number of
 * neighbors
 *
 * <p>We do this only where the bounding-boxes (dilated by 1 pixels) intersection. So as not to
 * waste computation-time in useless areas.
 *
 * @author Owen Feehan
 */
public class NumTouchingVoxelFaces extends TouchingVoxels {

    @Override
    protected double calcWithIntersection(
            ObjectMask object1, ObjectMask object2, BoundingBox bboxIntersect)
            throws FeatureCalcException {

        ObjectMask object2Relative = RelativeUtilities.createRelMask(object2, object1);

        try {
            return ApplyKernel.applyForCount(
                    createCountKernelMask(object1, object2Relative),
                    object1.getVoxelBox(),
                    RelativeUtilities.createRelBBox(bboxIntersect, object1));
        } catch (OperationFailedException e) {
            throw new FeatureCalcException(e);
        }
    }
}
