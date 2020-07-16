/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme;

import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;

/**
 * Before and after state, when a src is coverted in a CfgNRGPixelized
 *
 * @author Owen Feehan
 */
public class ToPixelized<T> {

    private T src;
    private CfgNRGPixelized dest;

    public ToPixelized(T src, CfgNRGPixelized dest) {
        super();
        this.src = src;
        this.dest = dest;
    }

    public T getSrc() {
        return src;
    }

    public CfgNRGPixelized getDest() {
        return dest;
    }
}
