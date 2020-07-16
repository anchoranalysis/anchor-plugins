/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.properties.ObjectWithProperties;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernel3;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

public class SurfaceSizeMaskNonZero extends FeatureSingleMemoRegion {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int maskIndex = 0;

    @BeanField @Getter @Setter private RegionMap regionMap = RegionMapSingleton.instance();

    @BeanField @Getter @Setter private boolean suppressZ = false;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputSingleMemo> input) throws FeatureCalcException {

        ObjectMask objectMask = createMask(input.get());
        int surfaceSize = estimateSurfaceSize(input.get().getPxlPartMemo(), objectMask);

        return resolveArea(surfaceSize, input.get().getResOptional());
    }

    private ObjectMask createMask(FeatureInputSingleMemo input) throws FeatureCalcException {
        ObjectWithProperties omWithProps =
                input.getPxlPartMemo()
                        .getMark()
                        .calcMask(
                                input.getDimensionsRequired(),
                                regionMap.membershipWithFlagsForIndex(getRegionID()),
                                BinaryValuesByte.getDefault());
        return omWithProps.getMask();
    }

    private int estimateSurfaceSize(VoxelizedMarkMemo pxlMarkMemo, ObjectMask object)
            throws FeatureCalcException {

        VoxelBox<ByteBuffer> vbOutline = calcOutline(object, !suppressZ);

        Extent extent = object.getBoundingBox().extent();

        try {
            int size = 0;
            for (int z = 0; z < extent.getZ(); z++) {
                VoxelStatistics stats = pxlMarkMemo.voxelized().statisticsFor(maskIndex, 0, z);
                if (stats.histogram().hasAboveZero()) {
                    size +=
                            vbOutline
                                    .extractSlice(z)
                                    .countEqual(object.getBinaryValues().getOnInt());
                }
            }
            return size;
        } catch (OperationFailedException e) {
            throw new FeatureCalcException(e);
        }
    }

    private static VoxelBox<ByteBuffer> calcOutline(ObjectMask object, boolean useZ) {
        OutlineKernel3 kernel = new OutlineKernel3(object.getBinaryValuesByte(), false, useZ);
        return ApplyKernel.apply(kernel, object.getVoxelBox(), object.getBinaryValuesByte());
    }
}
