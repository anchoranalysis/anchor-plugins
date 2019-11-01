package ch.ethz.biol.cell.mpp.pair.addcriteria;

import org.anchoranalysis.anchor.mpp.feature.addcriteria.AddCriteriaPair;
import org.anchoranalysis.anchor.mpp.feature.addcriteria.IncludeMarksFailureException;
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
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.session.FeatureSessionCreateParamsSubsession;

import ch.ethz.biol.cell.mpp.nrg.feature.session.FeatureSessionCreateParamsMPP;

public class AddCriteriaFeatureRelationThreshold extends AddCriteriaPair {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private Feature feature;
	
	@BeanField
	private double threshold;
	
	@BeanField
	private RelationBean relation;
	// END BEAN PROPERTIES

	@Override
	public boolean paramsEquals(Object other) {
		return false;
	}

	@Override
	public boolean includeMarks(
		PxlMarkMemo mark1,
		PxlMarkMemo mark2,
		ImageDim dim,
		FeatureSessionCreateParamsMPP session,
		boolean use3d
	) throws IncludeMarksFailureException {
		
		try {
			FeatureSessionCreateParamsSubsession subsession = session.createSubsession( session.getParamsFactory().createParams( mark1, mark2, dim )  );
			double featureVal = subsession.calc( feature);
			return relation.create().isRelationToValueTrue(featureVal, threshold);
			
		} catch (FeatureCalcException | CreateException e) {
			throw new IncludeMarksFailureException(e);
		}
	}

	public Feature getFeature() {
		return feature;
	}

	public void setFeature(Feature feature) {
		this.feature = feature;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public RelationBean getRelation() {
		return relation;
	}

	public void setRelation(RelationBean relation) {
		this.relation = relation;
	}

	@Override
	public FeatureList orderedListOfFeatures() {
		return new FeatureList(feature);
	}
}
