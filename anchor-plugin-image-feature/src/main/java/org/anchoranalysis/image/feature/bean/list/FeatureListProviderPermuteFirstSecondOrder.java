package org.anchoranalysis.image.feature.bean.list;

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


import java.util.ArrayList;

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.permute.property.PermutePropertySequenceInteger;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.input.FeatureInputNRGStack;
import org.anchoranalysis.plugin.operator.feature.bean.arithmetic.MultiplyByConstant;
import org.anchoranalysis.plugin.operator.feature.bean.order.range.IfOutsideRange;

import ch.ethz.biol.cell.mpp.nrg.feature.objmask.NRGParamThree;
import ch.ethz.biol.cell.mpp.nrg.feature.operator.FeatureFirstSecondOrder;

public abstract class FeatureListProviderPermuteFirstSecondOrder extends FeatureListProvider<FeatureInputNRGStack> {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private Feature<FeatureInputNRGStack> feature;
	
	@BeanField
	private PermutePropertySequenceInteger permuteProperty;
	
	@BeanField
	private String paramPrefix;
	
	/**
	 * If true the constant is appended to the param prefix (a dot and a number)
	 */
	@BeanField
	private boolean paramPrefixAppendNumber = true;
	// END BEAN PROPERTIES
	
	// Possible defaultInstances for beans......... saved from checkMisconfigured for delayed checks elsewhere
	private BeanInstanceMap defaultInstances;
	
	private CreateFirstSecondOrder<FeatureInputNRGStack> factory;
	private double minRange;
	private double maxRange;
	
	@FunctionalInterface
	public static interface CreateFirstSecondOrder<T extends FeatureInput> {
		FeatureFirstSecondOrder<T> create();
	}

	public FeatureListProviderPermuteFirstSecondOrder(CreateFirstSecondOrder<FeatureInputNRGStack> factory, double minRange, double maxRange ) {
		super();
		this.factory = factory;
		this.minRange = minRange;
		this.maxRange = maxRange;
	}
	
	
	@Override
	public void checkMisconfigured(BeanInstanceMap defaultInstances) throws BeanMisconfiguredException {
		super.checkMisconfigured(defaultInstances);
		this.defaultInstances = defaultInstances;
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
	
	public static Feature<FeatureInputNRGStack> createNRGParam( PermutePropertySequenceInteger permuteProperty, String paramPrefix, String suffix, boolean appendNumber ) {
		NRGParamThree paramMean = new NRGParamThree();
		paramMean.setIdLeft(paramPrefix);
		if (appendNumber) {
			paramMean.setIdMiddle( Integer.toString(permuteProperty.getSequence().getStart()) );
		} else {
			paramMean.setIdMiddle("");
		}
		paramMean.setIdRight(suffix);
		return paramMean;
	}
		
	private Feature<FeatureInputNRGStack> wrapInScore( Feature<FeatureInputNRGStack> feature ) {
		FeatureFirstSecondOrder<FeatureInputNRGStack> featureScore = factory.create();
		featureScore.setItem(feature);
		featureScore.setItemMean(
			createNRGParam(permuteProperty,paramPrefix,"_fitted_normal_mean",paramPrefixAppendNumber)
		);
		featureScore.setItemStdDev(
			createNRGParam(permuteProperty,paramPrefix,"_fitted_normal_sd",paramPrefixAppendNumber)
		);
		return featureScore;
	}
	
	private PermutePropertySequenceInteger configurePermuteProperty( PermutePropertySequenceInteger permuteProperty ) {
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
	
	@Override
	public FeatureList<FeatureInputNRGStack> create() throws CreateException {
		
		FeatureListProviderPermute<Integer, FeatureInputNRGStack> delegate = new FeatureListProviderPermute<>();
		
		// Wrap our feature in a gaussian score
		Feature<FeatureInputNRGStack> featureScore = feature.duplicateBean();
		featureScore = wrapInScore(featureScore);
		featureScore = wrapWithMultiplyByConstant(featureScore);
		featureScore = wrapWithMinMaxRange(featureScore);
		delegate.setFeature(featureScore);
				
		PermutePropertySequenceInteger permutePropertyConfigured = configurePermuteProperty(
			(PermutePropertySequenceInteger) permuteProperty.duplicateBean()
		);
		
		delegate.setListPermuteProperty( new ArrayList<>() );
		delegate.getListPermuteProperty().add( permutePropertyConfigured );
		
		try {
			delegate.checkMisconfigured( defaultInstances );
		} catch (BeanMisconfiguredException e) {
			throw new CreateException(e);
		}
		
		return delegate.create();
	}

	public PermutePropertySequenceInteger getPermuteProperty() {
		return permuteProperty;
	}

	public void setPermuteProperty(PermutePropertySequenceInteger permuteProperty) {
		this.permuteProperty = permuteProperty;
	}

	public Feature<FeatureInputNRGStack> getFeature() {
		return feature;
	}

	public void setFeature(Feature<FeatureInputNRGStack> feature) {
		this.feature = feature;
	}

	public String getParamPrefix() {
		return paramPrefix;
	}

	public void setParamPrefix(String paramPrefix) {
		this.paramPrefix = paramPrefix;
	}

	public boolean isParamPrefixAppendNumber() {
		return paramPrefixAppendNumber;
	}

	public void setParamPrefixAppendNumber(boolean paramPrefixAppendNumber) {
		this.paramPrefixAppendNumber = paramPrefixAppendNumber;
	}
}
