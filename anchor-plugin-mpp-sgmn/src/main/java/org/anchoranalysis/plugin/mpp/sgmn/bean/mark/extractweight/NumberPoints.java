package org.anchoranalysis.plugin.mpp.sgmn.bean.mark.extractweight;

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkPointList;

public class NumberPoints extends ExtractWeightFromMark {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
