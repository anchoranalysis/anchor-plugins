package ch.ethz.biol.cell.mpp.mark.provider;

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
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.session.SimpleSession;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.extent.ImageRes;

import ch.ethz.biol.cell.mpp.mark.Mark;
import ch.ethz.biol.cell.mpp.nrg.feature.mark.FeatureMarkParams;

public class MarkProviderRequireFeatureRelationThreshold extends MarkProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private MarkProvider markProvider;
	
	@BeanField
	private FeatureProvider featureProvider;
	
	@BeanField
	private double threshold;
	
	@BeanField
	private RelationBean relation;
	
	@BeanField @Optional
	private ImageDimProvider resProvider;
	// END BEAN PROPERTIES
	
	@Override
	public Mark create() throws CreateException {

		Mark mark = markProvider.create();
		
		if (mark==null) {
			return null;
		}
		
		FeatureInitParams initParams = new FeatureInitParams();
		
		Feature feature = featureProvider.create();
				
		ImageRes res = resProvider!=null ? resProvider.create().getRes() : null;
		
		FeatureMarkParams markParams = new FeatureMarkParams(mark, res);
		
		double featureVal;
		try {
			SimpleSession session = new SimpleSession();
			featureVal = session.calc( feature, initParams, getSharedObjects().getFeature().getSharedFeatureSet(), markParams, getLogger() );
			
		} catch (FeatureCalcException | InitException e) {
			throw new CreateException(e);
		}
		
		//System.out.printf("featureVal=%f\n", featureVal);
		
		if (relation.create().isRelationToValueTrue(featureVal, threshold)) {
			return mark;
		} else {
			return null;
		}
	}

	public MarkProvider getMarkProvider() {
		return markProvider;
	}


	public void setMarkProvider(MarkProvider markProvider) {
		this.markProvider = markProvider;
	}


	public FeatureProvider getFeatureProvider() {
		return featureProvider;
	}


	public void setFeatureProvider(FeatureProvider featureProvider) {
		this.featureProvider = featureProvider;
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

	public ImageDimProvider getResProvider() {
		return resProvider;
	}

	public void setResProvider(ImageDimProvider resProvider) {
		this.resProvider = resProvider;
	}
}
