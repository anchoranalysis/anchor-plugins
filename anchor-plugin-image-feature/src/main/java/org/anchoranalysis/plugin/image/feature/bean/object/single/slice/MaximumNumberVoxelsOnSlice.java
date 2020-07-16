/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.slice;

import java.nio.ByteBuffer;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * Maximum number of voxels in the object-mask across all slices.
 *
 * @author Owen Feehan
 */
public class MaximumNumberVoxelsOnSlice extends FeatureSingleObject {

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

        FeatureInputSingleObject params = input.get();

        int max = 0;

        for (int z = 0; z < params.getObject().getBoundingBox().extent().getZ(); z++) {
            ByteBuffer bb = params.getObject().getVoxelBox().getPixelsForPlane(z).buffer();
            int cnt = cntForByteBuffer(bb, params.getObject().getBinaryValuesByte().getOnByte());

            if (cnt > max) {
                max = cnt;
            }
        }
        return max;
    }

    private static int cntForByteBuffer(ByteBuffer bb, byte equalVal) {
        int cnt = 0;
        while (bb.hasRemaining()) {
            if (bb.get() == equalVal) {
                cnt++;
            }
        }
        return cnt;
    }
}
