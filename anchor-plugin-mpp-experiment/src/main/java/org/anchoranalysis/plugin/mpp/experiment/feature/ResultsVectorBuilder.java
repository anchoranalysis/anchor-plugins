package org.anchoranalysis.plugin.mpp.experiment.feature;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

import java.util.function.Function;

import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.results.ResultsVector;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.merged.FeatureInputPairObjsMerged;
import org.anchoranalysis.image.objmask.ObjMask;

class ResultsVectorBuilder {

	private boolean suppressErrors;
	private ErrorReporter errorReporter;
	private ResultsVector out;
	private int cnt;
		
	public ResultsVectorBuilder(int size, boolean suppressErrors, ErrorReporter errorReporter) {
		super();
		this.suppressErrors = suppressErrors;
		this.errorReporter = errorReporter;
		this.out = new ResultsVector(size);
		this.cnt = 0;
	}
	
	/** 
	 * Calculates and inserts a derived obj-mask params from a merged.
	 */
	public void calcAndInsert(
		FeatureInputPairObjsMerged params,
		Function<FeatureInputPairObjsMerged,ObjMask> extractObj,
		FeatureCalculatorMulti<FeatureInputSingleObj> session
	) throws FeatureCalcException {
		FeatureInputSingleObj paramsSpecific = new FeatureInputSingleObj(
			extractObj.apply(params)
		);
		//paramsSpecific.setNrgStack( params.getNrgStack() );  // This is necessary? Why?
		calcAndInsert(paramsSpecific, session);
	}
	
	/**
	 * Calculates the parameters belong to a particular session and inserts into a ResultsVector
	 * 
	 * @param params
	 * @param session
	 * @param start
	 * @param out
	 * @param errorReporter
	 * @return length(resultsVector)
	 * @throws FeatureCalcException
	 */
	public <T extends FeatureInput> void calcAndInsert( T params, FeatureCalculatorMulti<T> session ) throws FeatureCalcException {
		ResultsVector rvImage =  suppressErrors ? session.calcSuppressErrors( params, errorReporter ) : session.calc(params) ;
		out.set(cnt, rvImage);
		cnt += rvImage.length();
	}

	public ResultsVector getResultsVector() {
		return out;
	}
}
