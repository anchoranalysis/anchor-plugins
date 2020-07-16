/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.check;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

public class Not extends CheckMark {

    // START BEAN PROPERTIES
    @BeanField private CheckMark check;
    // END BEAN PROPERTIES

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return check.isCompatibleWith(testMark);
    }

    @Override
    public boolean check(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack)
            throws CheckException {
        return !check.check(mark, regionMap, nrgStack);
    }

    public CheckMark getCheck() {
        return check;
    }

    public void setCheck(CheckMark check) {
        this.check = check;
    }
}
