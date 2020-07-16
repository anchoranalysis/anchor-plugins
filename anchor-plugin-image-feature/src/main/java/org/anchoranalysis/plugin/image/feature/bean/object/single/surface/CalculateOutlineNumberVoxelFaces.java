/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.surface;

import java.nio.ByteBuffer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernelNeighborhoodIgnoreOutsideScene;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateOutlineNumberVoxelFaces
        extends FeatureCalculation<Integer, FeatureInputSingleObject> {

    /** Whether to calculate the outline on a MIP */
    private final boolean mip;

    /**
     * Whether to suppress 3D calculations (only consider XY neighbors). Doesn't make sense if
     * mip=TRUE, and will then be ignroed.
     */
    private final boolean suppress3D;

    private static int calcSurfaceSize(
            ObjectMask object, ImageDimensions dimensions, boolean mip, boolean suppress3D)
            throws OperationFailedException {

        boolean do3D = (dimensions.getZ() > 1) && !suppress3D;

        if (do3D && mip) {
            // If we're in 3D mode AND MIP mode, then we get a maximum intensity projection
            CountKernel kernel =
                    new CountKernelNeighborhoodIgnoreOutsideScene(
                            false,
                            object.getBinaryValuesByte(),
                            true,
                            dimensions.getExtent(),
                            object.getBoundingBox().cornerMin());

            VoxelBox<ByteBuffer> mipVb = object.getVoxelBox().maxIntensityProj();
            return ApplyKernel.applyForCount(kernel, mipVb);

        } else {
            CountKernel kernel =
                    new CountKernelNeighborhoodIgnoreOutsideScene(
                            do3D,
                            object.getBinaryValuesByte(),
                            true,
                            dimensions.getExtent(),
                            object.getBoundingBox().cornerMin());
            return ApplyKernel.applyForCount(kernel, object.getVoxelBox());
        }
    }

    @Override
    protected Integer execute(FeatureInputSingleObject params) throws FeatureCalcException {
        try {
            return calcSurfaceSize(
                    params.getObject(), params.getDimensionsRequired(), mip, suppress3D);
        } catch (OperationFailedException e) {
            throw new FeatureCalcException(e);
        }
    }
}
