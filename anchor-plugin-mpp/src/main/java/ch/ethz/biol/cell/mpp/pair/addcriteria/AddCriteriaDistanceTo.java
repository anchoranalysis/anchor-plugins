package ch.ethz.biol.cell.mpp.pair.addcriteria;

import org.anchoranalysis.anchor.mpp.feature.addcriteria.AddCriteriaPair;
import org.anchoranalysis.anchor.mpp.feature.addcriteria.IncludeMarksFailureException;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemPairCalcParams;
import org.anchoranalysis.anchor.mpp.feature.session.FeatureSessionCreateParamsMPP;
import org.anchoranalysis.anchor.mpp.mark.MarkDistance;
import org.anchoranalysis.anchor.mpp.mark.UnsupportedMarkTypeException;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;

/*
 * #%L
 * anchor-plugin-mpp
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.session.SequentialSession;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.extent.ImageDim;

public class AddCriteriaDistanceTo extends AddCriteriaPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7256594098140540214L;
	
	// START BEAN PROPERTIES
	@BeanField
	private UnitValueDistance threshold;
	
	@BeanField
	private MarkDistance distance;
	// END BEAN PROPERTIES
	
	public AddCriteriaDistanceTo() {
		
	}
	
	@Override
	public boolean includeMarks(PxlMarkMemo mark1, PxlMarkMemo mark2, ImageDim dim, SequentialSession<NRGElemPairCalcParams> session, boolean use3D) throws IncludeMarksFailureException {
		double d;
		try {
			d = distance.distance(mark1.getMark(), mark2.getMark());
		} catch (UnsupportedMarkTypeException e) {
			throw new IncludeMarksFailureException(e);
		}
		
		double thresholdVal = threshold.rslv(dim.getRes(), mark1.getMark().centerPoint(), mark2.getMark().centerPoint() );
		
		if (d < thresholdVal) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean paramsEquals(Object other) {
		
		if (!(other instanceof AddCriteriaDistanceTo)) {
			return false;
		}
		
		AddCriteriaDistanceTo obj = (AddCriteriaDistanceTo) other;
		
		if (threshold!=obj.threshold) {
			return false;
		}
		
		return true;
	}

	public MarkDistance getDistance() {
		return distance;
	}

	public void setDistance(MarkDistance distance) {
		this.distance = distance;
	}

	public UnitValueDistance getThreshold() {
		return threshold;
	}

	public void setThreshold(UnitValueDistance threshold) {
		this.threshold = threshold;
	}

	@Override
	public FeatureList orderedListOfFeatures() {
		return new FeatureList();
	}

	
}
