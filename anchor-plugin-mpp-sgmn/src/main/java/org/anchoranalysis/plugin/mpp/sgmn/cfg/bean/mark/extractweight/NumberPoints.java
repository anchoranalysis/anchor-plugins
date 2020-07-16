/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.mark.extractweight;

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.points.MarkPointList;

public class NumberPoints extends ExtractWeightFromMark {

    @Override
    public double weightFor(Mark mark) {
        MarkPointList cast = (MarkPointList) mark;
        return (double) cast.getPoints().size();
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof MarkPointList;
    }
}
