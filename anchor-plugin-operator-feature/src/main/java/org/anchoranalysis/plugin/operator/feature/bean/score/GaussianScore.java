package org.anchoranalysis.plugin.operator.feature.bean.score;

/*
 * #%L
 * anchor-feature
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
import org.anchoranalysis.core.cache.Operation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.plugin.operator.feature.score.GaussianScoreCalculator;


// A score between 0 and 1, based upon the CDF of a guassian. as one approaches the mean, the score approaches 1.0
public class GaussianScore<T extends FeatureInput> extends FeatureStatScore<T> {

	// START BEAN PROPERTIES
	@BeanField
	private boolean ignoreHigherSide = false;	// Always returns 1.0 for the higher side
	
	@BeanField
	private boolean ignoreLowerSide = false;	// Always returns 1.0 for the lower side
	
	@BeanField
	private boolean rewardHigherSide = false;	// Treat the higher side as if it's the the fill cdf
	
	@BeanField
	private boolean rewardLowerSide = false;	// Treat the lower side as if it's 1 - the fill cdf
	// END BEAN PROPERTIES
	
	@Override
	protected double deriveScore(double featureValue, double mean, Operation<Double,FeatureCalcException> stdDev) throws FeatureCalcException {
		
		if (ignoreHigherSide) {
			if (featureValue>mean) {
				return 1.0;
			}
		}
		
		if (ignoreLowerSide) {
			if (featureValue<mean) {
				return 1.0;
			}
		}
		
		return GaussianScoreCalculator.calc(
			mean,
			stdDev.doOperation(),
			featureValue,
			rewardHigherSide,
			rewardLowerSide
		);
	}
	
	public boolean isIgnoreHigherSide() {
		return ignoreHigherSide;
	}

	public void setIgnoreHigherSide(boolean ignoreHigherSide) {
		this.ignoreHigherSide = ignoreHigherSide;
	}

	public boolean isIgnoreLowerSide() {
		return ignoreLowerSide;
	}

	public void setIgnoreLowerSide(boolean ignoreLowerSide) {
		this.ignoreLowerSide = ignoreLowerSide;
	}

	public boolean isRewardHigherSide() {
		return rewardHigherSide;
	}

	public void setRewardHigherSide(boolean rewardHigherSide) {
		this.rewardHigherSide = rewardHigherSide;
	}

	public boolean isRewardLowerSide() {
		return rewardLowerSide;
	}

	public void setRewardLowerSide(boolean rewardLowerSide) {
		this.rewardLowerSide = rewardLowerSide;
	}
}
