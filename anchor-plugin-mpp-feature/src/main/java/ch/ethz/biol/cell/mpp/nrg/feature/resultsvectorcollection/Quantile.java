package ch.ethz.biol.cell.mpp.nrg.feature.resultsvectorcollection;

import org.anchoranalysis.anchor.mpp.feature.bean.results.FeatureResults;

/*
 * #%L
 * anchor-plugin-mpp-feature
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
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.ResultsVector;
import org.anchoranalysis.feature.calc.ResultsVectorCollection;
import org.anchoranalysis.feature.resultsvectorcollection.FeatureInputResults;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

public class Quantile extends FeatureResults {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private String id = "";
	
	@BeanField
	private double quantile = 0;
	
	/**
	 * If true, the quantile is interpreted as a percentage rather than a decimal
	 */
	@BeanField
	private boolean asPercentage = false;
	// END BEAN PROPERTIES

	@Override
	public double calc(FeatureInputResults params)
			throws FeatureCalcException {

		try {
			DoubleArrayList featureVals = new DoubleArrayList();
			
			int index = params.getFeatureNameIndex().indexOf(id);
			
			ResultsVectorCollection rvc = params.getResultsVectorCollection();
			
			if (rvc.size()==0) {
				throw new FeatureCalcException("There are 0 items");
			}
			
			for (int i=0; i<rvc.size(); i++) {
				ResultsVector rv = rvc.get(i);
				assert(rv!=null);
				featureVals.add(rv.get(index));
			}
			
			featureVals.sort();
			
			double quantileFinal = asPercentage ? quantile/100 : quantile;
			return Descriptive.quantile(featureVals, quantileFinal);
			
		} catch (GetOperationFailedException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	
	public double getQuantile() {
		return quantile;
	}

	public void setQuantile(double quantile) {
		this.quantile = quantile;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isAsPercentage() {
		return asPercentage;
	}

	public void setAsPercentage(boolean asPercentage) {
		this.asPercentage = asPercentage;
	}


}
