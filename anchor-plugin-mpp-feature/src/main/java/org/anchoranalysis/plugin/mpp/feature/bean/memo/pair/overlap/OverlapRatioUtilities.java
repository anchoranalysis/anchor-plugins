/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.overlap;

import java.util.function.LongBinaryOperator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class OverlapRatioUtilities {

    /** Returns {@link Math::max} or {@link Math::min} depending on a flag */
    public static LongBinaryOperator maxOrMin(boolean useMax) {
        return useMax ? Math::max : Math::min;
    }

    public static double calcOverlapRatio(
            VoxelizedMarkMemo obj1,
            VoxelizedMarkMemo obj2,
            double overlap,
            int regionID,
            boolean mip,
            LongBinaryOperator funcAgg) {

        if (overlap == 0.0) {
            return 0.0;
        }

        if (mip) {
            return overlap;
        } else {
            double volume = calcVolumeAgg(obj1, obj2, regionID, funcAgg);
            return overlap / volume;
        }
    }

    private static double calcVolumeAgg(
            VoxelizedMarkMemo obj1,
            VoxelizedMarkMemo obj2,
            int regionID,
            LongBinaryOperator funcAgg) {
        long size1 = sizeFromMemo(obj1, regionID);
        long size2 = sizeFromMemo(obj2, regionID);
        return funcAgg.applyAsLong(size1, size2);
    }

    private static long sizeFromMemo(VoxelizedMarkMemo obj, int regionID) {
        return obj.voxelized().statisticsForAllSlices(0, regionID).size();
    }
}
