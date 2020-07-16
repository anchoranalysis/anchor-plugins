/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.distance;

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.FeaturePairMemoSingleRegion;

/**
 * Measures the amount of distance in Z for the bounding box.
 *
 * <p>This is useful for measuring how much two objects overlap in Z.
 *
 * <p>It is only calculated if there is overlap of the bounding boxes in XYZ, else 0 is returned.
 *
 * @author Owen Feehan
 */
public class BoundingBoxZDistance extends FeaturePairMemoSingleRegion {

    @Override
    public double calc(SessionInput<FeatureInputPairMemo> input) throws FeatureCalcException {

        FeatureInputPairMemo inputSessionless = input.get();

        BoundingBox bbox1 = bbox(inputSessionless, FeatureInputPairMemo::getObj1);
        BoundingBox bbox2 = bbox(inputSessionless, FeatureInputPairMemo::getObj2);

        // Check the bounding boxes intersect in general (including XY)
        if (bbox1.intersection().existsWith(bbox2)) {
            return 0.0;
        }

        return calcZDistance(bbox1, bbox2);
    }

    private double calcZDistance(BoundingBox bbox1, BoundingBox bbox2) {
        int z1Min = bbox1.cornerMin().getZ();
        int z1Max = bbox1.calcCornerMax().getZ();

        int z2Min = bbox2.cornerMin().getZ();
        int z2Max = bbox2.calcCornerMax().getZ();

        int diff1 = Math.abs(z1Min - z2Min);
        int diff2 = Math.abs(z1Min - z2Max);
        int diff3 = Math.abs(z2Min - z1Max);
        int diff4 = Math.abs(z2Min - z1Min);

        int min1 = Math.min(diff1, diff2);
        int min2 = Math.min(diff3, diff4);
        return Math.min(min1, min2);
    }
}
