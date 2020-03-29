package org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme;

import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;

/**
 * A transformation of one CfgNRGPixelized into another
 * 
 * @author FEEHANO
 *
 */
public class DualCfgNRGPixelized {

	private CfgNRGPixelized src;
	private CfgNRGPixelized dest;

	public DualCfgNRGPixelized(CfgNRGPixelized src, CfgNRGPixelized dest) {
		super();
		this.src = src;
		this.dest = dest;
	}

	public CfgNRGPixelized getSrc() {
		return src;
	}

	public CfgNRGPixelized getDest() {
		return dest;
	}
}
