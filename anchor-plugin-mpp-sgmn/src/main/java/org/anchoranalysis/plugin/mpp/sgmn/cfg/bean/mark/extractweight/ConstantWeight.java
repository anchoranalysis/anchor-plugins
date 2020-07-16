/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.mark.extractweight;

import org.anchoranalysis.anchor.mpp.mark.Mark;

/** All marks have weight of 1 */
public class ConstantWeight extends ExtractWeightFromMark {

    @Override
    public double weightFor(Mark mark) {
        return 1.0;
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }
}
