package org.anchoranalysis.image.feature.bean.list;

/*
 * #%L
 * anchor-plugin-image-feature
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

import ch.ethz.biol.cell.mpp.nrg.feature.operator.DivideExplicit;

/**
 * Similar to FeatureListProviderPermute but embeds the feature in a GaussianScore
 * 
 * @author Owen Feehan
 *
 */
public class FeatureListProviderPermuteDivideByParam extends FeatureListProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private Feature feature;
	
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
	
	@Override
	public void checkMisconfigured(BeanInstanceMap defaultInstances) throws BeanMisconfiguredException {
		super.checkMisconfigured(defaultInstances);
		this.defaultInstances = defaultInstances;
	}

	private Feature wrapInDivide( Feature feature ) {
		DivideExplicit featureScore = new DivideExplicit();
		featureScore.setItem1(feature);
		featureScore.setItem2( FeatureListProviderPermuteGaussianScore.createNRGParam(permuteProperty,paramPrefix,"_median",paramPrefixAppendNumber) );
		return featureScore;
	}
	
	private PermutePropertySequenceInteger configurePermuteProperty( PermutePropertySequenceInteger permuteProperty ) {
		if (permuteProperty.getAdditionalPropertyPaths()==null) {
			permuteProperty.setAdditionalPropertyPaths( new StringSet() );
		}
		
		if (paramPrefixAppendNumber) {
			permuteProperty.getAdditionalPropertyPaths().add("item2.idMiddle");
		}
		
		permuteProperty.setPropertyPath(
			String.format("item1.%s", permuteProperty.getPropertyPath() )
		);
		return permuteProperty;
	}
	
	@Override
	public FeatureList create() throws CreateException {
		
		FeatureListProviderPermute<Integer> delegate = new FeatureListProviderPermute<>();
		
		// Wrap our feature in a gaussian score
		Feature featureScore = feature.duplicateBean();
		featureScore = wrapInDivide(featureScore);
		delegate.setFeature(featureScore);
				
		PermutePropertySequenceInteger permutePropertyConfigured = configurePermuteProperty(
			(PermutePropertySequenceInteger) permuteProperty.duplicateBean()
		);
		
		delegate.setListPermuteProperty( new ArrayList<>() );
		
		delegate.getListPermuteProperty().add( permutePropertyConfigured );
		
		try {
			delegate.checkMisconfigured(defaultInstances);
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

	public Feature getFeature() {
		return feature;
	}

	public void setFeature(Feature feature) {
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