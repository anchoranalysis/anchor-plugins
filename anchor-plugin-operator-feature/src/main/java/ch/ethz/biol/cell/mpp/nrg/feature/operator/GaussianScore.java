package ch.ethz.biol.cell.mpp.nrg.feature.operator;

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
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureInput;

import cern.jet.random.Normal;


// A score between 0 and 1, based upon the CDF of a guassian. as one approaches the mean, the score approaches 1.0
public class GaussianScore<T extends FeatureInput> extends FeatureFirstSecondOrder<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
		
	public static double calc( double mean, double stdDev, double val, boolean rewardHigherSide, boolean rewardLowerSide ) {
		Normal normal = new Normal(mean, stdDev, null );
		double cdf = normal.cdf(val);
		
		if (rewardHigherSide) {
			return cdf;
		}
		
		if (rewardLowerSide) {
			return (1-cdf);
		}
		
		if (val>mean) {
			return (1-cdf)*2;
		} else {
			return cdf*2;
		}
	}
	
	@Override
	public double calc( SessionInput<T> input ) throws FeatureCalcException {
		
		double val = input.calc( getItem() );
		double mean = input.calc( getItemMean() );
		
		if (ignoreHigherSide) {
			if (val>mean) {
				return 1.0;
			}
		}
		
		if (ignoreLowerSide) {
			if (val<mean) {
				return 1.0;
			}
		}
		
		double stdDev = input.calc( getItemStdDev() );
		
		return calc( mean, stdDev, val, rewardHigherSide, rewardLowerSide );
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
