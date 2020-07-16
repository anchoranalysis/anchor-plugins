/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.mark.extractweight;

import org.anchoranalysis.anchor.mpp.mark.CompatibleWithMark;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.AnchorBean;

public abstract class ExtractWeightFromMark extends AnchorBean<ExtractWeightFromMark>
        implements CompatibleWithMark {

    public abstract double weightFor(Mark mark);
}
