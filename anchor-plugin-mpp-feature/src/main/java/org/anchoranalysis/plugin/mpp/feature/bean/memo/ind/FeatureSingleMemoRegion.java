/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeatureSingleMemo;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.plugin.mpp.feature.bean.unit.UnitConverter;

public abstract class FeatureSingleMemoRegion extends FeatureSingleMemo {

    // START BEAN PROPERTIES
    @BeanField private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;

    @BeanField private UnitConverter unit = new UnitConverter();
    // END BEAN PROPERTIES

    public int getRegionID() {
        return regionID;
    }

    public void setRegionID(int regionID) {
        this.regionID = regionID;
    }

    public UnitConverter getUnit() {
        return unit;
    }

    public void setUnit(UnitConverter unit) {
        this.unit = unit;
    }

    protected double resolveVolume(double value, Optional<ImageResolution> res)
            throws FeatureCalcException {
        return unit.resolveVolume(value, res);
    }

    protected double resolveArea(double value, Optional<ImageResolution> res)
            throws FeatureCalcException {
        return unit.resolveArea(value, res);
    }
}
