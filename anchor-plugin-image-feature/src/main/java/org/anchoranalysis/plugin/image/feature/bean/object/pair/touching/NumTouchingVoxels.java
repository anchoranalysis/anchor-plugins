/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.pair.touching;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernelNeighborhoodMask;

/**
 * A scheme for counting touching voxels.
 *
 * <p>A voxel in the second object is deemed touching if it has 4-connectivity with a voxel on the
 * exterior of the first-object (source)
 *
 * <p>In practice, we do this only where the bounding-boxes (dilated by 1 pixels) intersection, so
 * as to reduce computation-time.
 *
 * @author Owen Feehan
 */
public class NumTouchingVoxels extends TouchingVoxels {

    @Override
    protected double calcWithIntersection(
            ObjectMask object1, ObjectMask object2, BoundingBox bboxIntersect)
            throws FeatureCalcException {
        // As this means of measuring the touching pixels can differ slightly depending on om1->om2
        // or om2->om1, it's done in both directions.
        try {
            return Math.max(
                    numTouchingFrom(object1, object2, bboxIntersect),
                    numTouchingFrom(object2, object1, bboxIntersect));

        } catch (OperationFailedException e) {
            throw new FeatureCalcException(e);
        }
    }

    private int numTouchingFrom(
            ObjectMask source, ObjectMask destination, BoundingBox bboxIntersect)
            throws OperationFailedException {
        BoundingBox bboxIntersectRelative = RelativeUtilities.createRelBBox(bboxIntersect, source);
        return calcNeighborhoodTouchingPixels(source, destination, bboxIntersectRelative);
    }

    private int calcNeighborhoodTouchingPixels(
            ObjectMask source, ObjectMask destination, BoundingBox bboxIntersectRelative)
            throws OperationFailedException {

        CountKernelNeighborhoodMask kernelMatch =
                new CountKernelNeighborhoodMask(
                        isDo3D(),
                        source.getBinaryValuesByte(),
                        RelativeUtilities.createRelMask(destination, source),
                        false);
        return ApplyKernel.applyForCount(kernelMatch, source.getVoxelBox(), bboxIntersectRelative);
    }
}
