package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.extractscoresize;

import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.DualCfgNRGPixelized;

public class DualCfgNRGPixelizedExtracter extends ExtractScoreSize<DualCfgNRGPixelized> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CfgNRGPixelizedExtracter helper = new CfgNRGPixelizedExtracter();;
	
	@Override
	public double extractScore(DualCfgNRGPixelized item) {
		return helper.extractScore(item.getDest());
	}

	@Override
	public int extractSize(DualCfgNRGPixelized item) {
		return helper.extractSize(item.getDest());
	}

}
