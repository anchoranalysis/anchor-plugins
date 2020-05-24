package org.anchoranalysis.image.feature.bean.permute;

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


import java.util.Arrays;

import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.permute.property.PermutePropertySequenceInteger;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.input.FeatureInputNRGStack;
import org.anchoranalysis.plugin.operator.feature.bean.arithmetic.Divide;

/**
 * Similar to FeatureListProviderPermute but embeds the feature in a GaussianScore
 * 
 * @author Owen Feehan
 * @param T feature-input
 */
public class FeatureListProviderPermuteDivideByParam extends FeatureListProviderPermuteParamBase {
	
	@Override
	protected FeatureListProviderPermute<Integer,FeatureInputNRGStack> createDelegate(Feature<FeatureInputNRGStack> feature) throws CreateException {
		
		FeatureListProviderPermute<Integer,FeatureInputNRGStack> delegate = new FeatureListProviderPermute<>();
		
		// Wrap our feature in a Divide
		delegate.setFeature(
			wrapInDivide(feature.duplicateBean())
		);
				
		return delegate;
	}
	
	@Override
	protected PermutePropertySequenceInteger configurePermuteProperty( PermutePropertySequenceInteger permuteProperty ) {
		if (permuteProperty.getAdditionalPropertyPaths()==null) {
			permuteProperty.setAdditionalPropertyPaths( new StringSet() );
		}
		permuteProperty.setPropertyPath(
			String.format("list.%s", permuteProperty.getPropertyPath() )	// This will change the first item in the list
		);
		return permuteProperty;
	}
	
	private Feature<FeatureInputNRGStack> wrapInDivide( Feature<FeatureInputNRGStack> feature ) {
		Divide<FeatureInputNRGStack> featureScore = new Divide<>();
		featureScore.setList(
			Arrays.asList(
				feature,
				createNRGParam(
					"_median",
					false
				)	
			)
		);
		return featureScore;
	}
}