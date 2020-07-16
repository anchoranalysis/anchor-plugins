/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.pair.touching;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernel;

/**
 * A simple scheme for counting the touching voxels.
 *
 * <p>A voxel in the second object is touching if it has 4-connectivity with a voxel on the exterior
 * of the first-object (source)
 *
 * <p>In practice, we do this only where the bounding-boxes (dilated by 1 pixels) intersect. So as
 * not to waste computation-time in useless areas.
 *
 * @author Owen Feehan
 */
public class HasTouchingVoxels extends TouchingVoxels {

    @Override
    protected double calcWithIntersection(
            ObjectMask first, ObjectMask second, BoundingBox bboxIntersect)
            throws FeatureCalcException {
        return convertToInt(
                calculateHasTouchingRelative(
                        first,
                        RelativeUtilities.createRelMask(second, first),
                        RelativeUtilities.createRelBBox(bboxIntersect, first)));
    }

    private boolean calculateHasTouchingRelative(
            ObjectMask first, ObjectMask secondRelative, BoundingBox bboxIntersectRel)
            throws FeatureCalcException {
        CountKernel kernelMatch = createCountKernelMask(first, secondRelative);
        try {
            return ApplyKernel.applyUntilPositive(
                    kernelMatch, first.getVoxelBox(), bboxIntersectRel);
        } catch (OperationFailedException e) {
            throw new FeatureCalcException(e);
        }
    }

    private static int convertToInt(boolean b) {
        return b ? 1 : 0;
    }
}
