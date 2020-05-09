package org.anchoranalysis.plugin.mpp.bean.proposer.scalar;

import org.anchoranalysis.anchor.mpp.bean.proposer.ScalarProposer;

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
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageRes;

import cern.jet.random.Normal;

public class GaussianSamplerFromParams extends ScalarProposer {

	// START BEAN PROPERTIES
	@BeanField
	private KeyValueParamsProvider keyValueParamsProvider;
	
	@BeanField
	private String paramMean = "";
	
	@BeanField
	private String paramStdDev = "";
	
	@BeanField
	private double factorStdDev = 1.0;		// Multiples the standard deviation by a factor
	// END BEAN PROPERTIES
	
	@Override
	public double propose( RandomNumberGenerator re, ImageRes res ) throws OperationFailedException {
		
		try {
			KeyValueParams kvp = keyValueParamsProvider.create();
			assert(kvp != null);
			
			if (!kvp.containsKey(getParamMean())) {
				throw new OperationFailedException( String.format("Params are missing key '%s' for paramMean", getParamMean() ) );
			}
			
			if (!kvp.containsKey(getParamStdDev())) {
				throw new OperationFailedException( String.format("Params are missing key '%s' for paramStdDev", getParamStdDev() ) );
			}		
			
			double mean = Double.valueOf( kvp.getProperty(getParamMean()) );
			double sd = Double.valueOf( kvp.getProperty(getParamStdDev()) ) * factorStdDev;
			
			Normal normal = re.generateNormal(mean, sd);
			return normal.nextDouble();
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
	}
	
	public KeyValueParamsProvider getKeyValueParamsProvider() {
		return keyValueParamsProvider;
	}

	public void setKeyValueParamsProvider(
			KeyValueParamsProvider keyValueParamsProvider) {
		this.keyValueParamsProvider = keyValueParamsProvider;
	}

	public String getParamMean() {
		return paramMean;
	}

	public void setParamMean(String paramMean) {
		this.paramMean = paramMean;
	}

	public String getParamStdDev() {
		return paramStdDev;
	}

	public void setParamStdDev(String paramStdDev) {
		this.paramStdDev = paramStdDev;
	}

	public double getFactorStdDev() {
		return factorStdDev;
	}

	public void setFactorStdDev(double factorStdDev) {
		this.factorStdDev = factorStdDev;
	}

}
