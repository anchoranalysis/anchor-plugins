/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeatureSingleMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.MarkConic;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;

public class BBoxRatio extends FeatureSingleMemo {

    @Override
    public double calc(SessionInput<FeatureInputSingleMemo> input) throws FeatureCalcException {

        MarkConic markCast = (MarkConic) input.get().getPxlPartMemo().getMark();

        ImageDimensions dimensions = input.get().getDimensionsRequired();

        BoundingBox bb = markCast.bbox(dimensions, GlobalRegionIdentifiers.SUBMARK_INSIDE);

        int[] extent = bb.extent().createOrderedArray();

        // Let's change the z-dimension to include the relative-resolution
        extent[2] = (int) (bb.extent().getZ() * dimensions.getRes().getZRelativeResolution());

        int len = extent.length;
        assert (len >= 2);

        if (len == 2) {
            return ((double) extent[1]) / extent[0];
        } else {
            return ((double) extent[2]) / extent[0];
        }
    }
}
