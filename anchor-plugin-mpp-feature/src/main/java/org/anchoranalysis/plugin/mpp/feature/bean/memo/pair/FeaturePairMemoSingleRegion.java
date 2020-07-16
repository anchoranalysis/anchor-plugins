/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair;

import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.CalculateOverlap;
import java.util.function.Function;
import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeaturePairMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;

public abstract class FeaturePairMemoSingleRegion extends FeaturePairMemo {

    // START BEAN PROPERTIES
    @BeanField private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
    // END BEAN PROPERTIES

    protected double overlappingNumVoxels(SessionInput<FeatureInputPairMemo> input)
            throws FeatureCalcException {
        return input.calc(new CalculateOverlap(regionID));
    }

    protected BoundingBox bbox(
            FeatureInputPairMemo input,
            Function<FeatureInputPairMemo, VoxelizedMarkMemo> funcExtract)
            throws FeatureCalcException {
        ImageDimensions sd = input.getDimensionsRequired();
        return funcExtract.apply(input).getMark().bbox(sd, getRegionID());
    }

    public int getRegionID() {
        return regionID;
    }

    public void setRegionID(int regionID) {
        this.regionID = regionID;
    }
}
