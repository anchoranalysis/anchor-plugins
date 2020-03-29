package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.extractscoresize;

import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.ToPixelized;

public class ToPixelizedExtracter<T> extends ExtractScoreSize<ToPixelized<T>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CfgNRGPixelizedExtracter helper = new CfgNRGPixelizedExtracter();
	
	@Override
	public double extractScore(ToPixelized<T> item) {
		return helper.extractScore(item.getDest());
	}

	@Override
	public int extractSize(ToPixelized<T> item) {
		return helper.extractSize(item.getDest());
	}

}
