package ch.ethz.biol.cell.mpp.mark.provider;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.provider.MarkProvider;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.mark.Mark;

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
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.extent.ImageDim;

public class MarkProviderRequireFeatureRelationThreshold extends MarkProvider {

	// START BEAN PROPERTIES
	@BeanField
	private MarkProvider markProvider;
	
	@BeanField
	private FeatureProvider<FeatureInputMark> featureProvider;
	
	@BeanField
	private double threshold;
	
	@BeanField
	private RelationBean relation;
	
	@BeanField @OptionalBean
	private ImageDimProvider dimProvider;
	// END BEAN PROPERTIES
	
	@Override
	public Mark create() throws CreateException {

		Mark mark = markProvider.create();
		
		if (mark==null) {
			return null;
		}
		
		Feature<FeatureInputMark> feature = featureProvider.create();
		
		double featureVal = calculateInput(feature, mark);
		
		if (relation.create().isRelationToValueTrue(featureVal, threshold)) {
			return mark;
		} else {
			return null;
		}
	}
	
	private Optional<ImageDim> dimensions() throws CreateException {
		if (dimProvider!=null) {
			return Optional.of(
				dimProvider.create()
			);
		} else {
			 return Optional.empty();
		}
	}
	
	private double calculateInput( Feature<FeatureInputMark> feature, Mark mark ) throws CreateException {

		FeatureInputMark input = new FeatureInputMark(
			mark,
			dimensions()
		);
		
		try {
			FeatureCalculatorSingle<FeatureInputMark> session = FeatureSession.with(
				feature,
				new FeatureInitParams(),
				getSharedObjects().getFeature().getSharedFeatureSet(),
				getLogger()
			);
			return session.calc( input );
			
		} catch (FeatureCalcException e) {
			throw new CreateException(e);
		}
	}

	public MarkProvider getMarkProvider() {
		return markProvider;
	}


	public void setMarkProvider(MarkProvider markProvider) {
		this.markProvider = markProvider;
	}


	public FeatureProvider<FeatureInputMark> getFeatureProvider() {
		return featureProvider;
	}


	public void setFeatureProvider(FeatureProvider<FeatureInputMark> featureProvider) {
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

	public ImageDimProvider getDimProvider() {
		return dimProvider;
	}

	public void setDimProvider(ImageDimProvider dimProvider) {
		this.dimProvider = dimProvider;
	}
}
