/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.check;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

public class AndList extends CheckMark {

    // START BEAN PROPERTIES
    @BeanField private List<CheckMark> list = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public boolean check(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack)
            throws CheckException {

        for (CheckMark item : list) {
            if (!item.check(mark, regionMap, nrgStack)) {
                return false;
            }
        }
        return true;
    }

    public List<CheckMark> getList() {
        return list;
    }

    public void setList(List<CheckMark> list) {
        this.list = list;
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        for (CheckMark item : list) {
            if (!item.isCompatibleWith(testMark)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void start(NRGStackWithParams nrgStack) throws OperationFailedException {
        super.start(nrgStack);
        for (CheckMark item : list) {
            item.start(nrgStack);
        }
    }

    @Override
    public void end() {
        super.end();
        for (CheckMark item : list) {
            item.end();
        }
    }
}
