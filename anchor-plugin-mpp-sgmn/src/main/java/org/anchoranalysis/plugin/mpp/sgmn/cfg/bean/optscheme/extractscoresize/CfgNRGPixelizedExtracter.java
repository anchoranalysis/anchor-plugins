/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.extractscoresize;

import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;

public class CfgNRGPixelizedExtracter extends ExtractScoreSize<CfgNRGPixelized> {

    @Override
    public double extractScore(CfgNRGPixelized item) {

        if (item == null) {
            return Double.NaN;
        }

        return item.getTotal();
    }

    @Override
    public int extractSize(CfgNRGPixelized item) {

        if (item == null) {
            return 0;
        }

        return item.getCfg().size();
    }
}
