/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.check;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkConic;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

public class RadiiMinRatio extends CheckMark {

    // START BEAN PROPERTIES
    @BeanField private double min = 1;
    // END BEAN PROPERTIES

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof MarkConic;
    }

    @Override
    public boolean check(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack) {

        MarkConic markCast = (MarkConic) mark;
        double[] radiiOrdered = markCast.radiiOrderedResolved(nrgStack.getDimensions().getRes());

        int len = radiiOrdered.length;
        assert (len >= 2);

        assert (radiiOrdered[1] > radiiOrdered[0]);
        if (len == 3) {
            assert (radiiOrdered[2] > radiiOrdered[1]);
        }

        double ratio = radiiOrdered[len - 1] / radiiOrdered[0];
        ratio = Math.max(ratio, 1 / ratio);

        return (ratio >= min);
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }
}
