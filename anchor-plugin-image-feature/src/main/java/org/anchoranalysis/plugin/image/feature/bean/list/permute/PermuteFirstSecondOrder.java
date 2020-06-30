package org.anchoranalysis.plugin.image.feature.bean.list.permute;

/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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


import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.permute.property.PermutePropertySequenceInteger;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.input.FeatureInputNRGStack;
import org.anchoranalysis.plugin.operator.feature.bean.arithmetic.MultiplyByConstant;
import org.anchoranalysis.plugin.operator.feature.bean.range.IfOutsideRange;
import org.anchoranalysis.plugin.operator.feature.bean.score.FeatureStatScore;

public abstract class PermuteFirstSecondOrder extends PermuteFeatureSequenceInteger {

	// START BEAN PROPERTIES
	/**
	 * If true the constant is appended to the param prefix (a dot and a number)
	 */
	@BeanField
	private boolean paramPrefixAppendNumber = true;
	// END BEAN PROPERTIES
	
	private CreateFirstSecondOrder<FeatureInputNRGStack> factory;
	private double minRange;
	private double maxRange;
	
	@FunctionalInterface
	public static interface CreateFirstSecondOrder<T extends FeatureInput> {
		FeatureStatScore<T> create();
	}

	public PermuteFirstSecondOrder(CreateFirstSecondOrder<FeatureInputNRGStack> factory, double minRange, double maxRange ) {
		super();
		this.factory = factory;
		this.minRange = minRange;
		this.maxRange = maxRange;
	}
	
	@Override
	protected PermuteFeature<Integer,FeatureInputNRGStack> createDelegate(Feature<FeatureInputNRGStack> feature) throws CreateException {
		
		PermuteFeature<Integer, FeatureInputNRGStack> delegate = new PermuteFeature<>();
		
		// Wrap our feature in a gaussian score
		Feature<FeatureInputNRGStack> featureScore = feature.duplicateBean();
		featureScore = wrapInScore(featureScore);
		featureScore = wrapWithMultiplyByConstant(featureScore);
		featureScore = wrapWithMinMaxRange(featureScore);
		delegate.setFeature(featureScore);

		return delegate;
	}
	

	private static Feature<FeatureInputNRGStack> wrapWithMultiplyByConstant( Feature<FeatureInputNRGStack> feature ) {
		MultiplyByConstant<FeatureInputNRGStack> out = new MultiplyByConstant<>();
		out.setItem(feature);
		out.setValue(1);
		return out;
	}

	private Feature<FeatureInputNRGStack> wrapWithMinMaxRange( Feature<FeatureInputNRGStack> feature ) {
		IfOutsideRange<FeatureInputNRGStack> out = new IfOutsideRange<>();
		out.setItem(feature);
		out.setMin(minRange);
		out.setMax(maxRange);
		out.setBelowMinValue(minRange);
		out.setAboveMaxValue(maxRange);
		return out;
	}
		
	private Feature<FeatureInputNRGStack> wrapInScore( Feature<FeatureInputNRGStack> feature ) {
		FeatureStatScore<FeatureInputNRGStack> featureScore = factory.create();
		featureScore.setItem(feature);
		featureScore.setItemMean(
			createNRGParam("_fitted_normal_mean",paramPrefixAppendNumber)
		);
		featureScore.setItemStdDev(
			createNRGParam("_fitted_normal_sd",paramPrefixAppendNumber)
		);
		return featureScore;
	}
	
	@Override
	protected PermutePropertySequenceInteger configurePermuteProperty( PermutePropertySequenceInteger permuteProperty ) {
		if (permuteProperty.getAdditionalPropertyPaths()==null) {
			permuteProperty.setAdditionalPropertyPaths( new StringSet() );
		}
		
		if (paramPrefixAppendNumber) {
			permuteProperty.getAdditionalPropertyPaths().add("item.item.itemMean.idMiddle");
			permuteProperty.getAdditionalPropertyPaths().add("item.item.itemStdDev.idMiddle");
		}
		
		permuteProperty.setPropertyPath(
			String.format("item.item.item.%s", permuteProperty.getPropertyPath() )
		);
		return permuteProperty;
	}

	public boolean isParamPrefixAppendNumber() {
		return paramPrefixAppendNumber;
	}

	public void setParamPrefixAppendNumber(boolean paramPrefixAppendNumber) {
		this.paramPrefixAppendNumber = paramPrefixAppendNumber;
	}
}
