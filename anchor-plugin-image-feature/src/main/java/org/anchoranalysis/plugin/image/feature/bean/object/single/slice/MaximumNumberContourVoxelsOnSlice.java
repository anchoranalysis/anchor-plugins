/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.slice;

import java.nio.ByteBuffer;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.outline.FindOutline;

/**
 * Maximum number of voxels on any slice's contour (edge voxels) across all slices.
 *
 * @author Owen Feehan
 */
public class MaximumNumberContourVoxelsOnSlice extends FeatureSingleObject {

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {
        ObjectMask object = input.get().getObject();

        return numVoxelsOnContour(sliceWithMaxNumVoxels(object));
    }

    private static ObjectMask sliceWithMaxNumVoxels(ObjectMask obj) {
        return obj.extractSlice(indexOfSliceWithMaxNumVoxels(obj), false);
    }

    private static int numVoxelsOnContour(ObjectMask obj) {
        return FindOutline.outline(obj, 1, true, false).binaryVoxelBox().countOn();
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

    private static int indexOfSliceWithMaxNumVoxels(ObjectMask object) {

        int max = 0;
        int ind = 0;

        for (int z = 0; z < object.getBoundingBox().extent().getZ(); z++) {
            ByteBuffer bb = object.getVoxelBox().getPixelsForPlane(z).buffer();
            int cnt = cntForByteBuffer(bb, object.getBinaryValuesByte().getOnByte());

            if (cnt > max) {
                max = cnt;
                ind = z;
            }
        }
        return ind;
    }
}
