package org.anchoranalysis.test.feature.plugins;

/*-
 * #%L
 * anchor-test-feature-plugins
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

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.shared.SharedFeatureMulti;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.feature.init.FeatureInitParamsSharedObjs;
import org.anchoranalysis.test.LoggingFixture;


public class FeatureTestCalculator {

	private FeatureTestCalculator() {
		
	}
	
	public static <T extends FeatureInput> void assertDoubleResult( String message, Feature<T> feature, T params, double expectedResult ) throws FeatureCalcException, InitException {
		assertDoubleResult(message, feature, params, Optional.empty(), expectedResult);
	}
	
	public static <T extends FeatureInput> void assertIntResult( String message, Feature<T> feature, T params, int expectedResult ) throws FeatureCalcException, InitException {
		assertIntResult(message, feature, params, Optional.empty(), expectedResult);
	}
	
	public static <T extends FeatureInput> void assertDoubleResult( String message, Feature<T> feature, T params, Optional<ImageInitParams> imageInit, double expectedResult ) throws FeatureCalcException {
		assertResultTolerance(
			message,
			feature,
			params,
			createInitParams(imageInit),
			expectedResult,
			1e-4
		);
	}
	
	public static <T extends FeatureInput> void assertIntResult( String message, Feature<T> feature, T params, Optional<ImageInitParams> imageInit, int expectedResult ) throws FeatureCalcException {
		assertResultTolerance(
			message,
			feature,
			params,
			createInitParams(imageInit),			
			expectedResult,
			1e-20
		);
	}
	
	private static FeatureInitParams createInitParams( Optional<ImageInitParams> imageInit ) {
		Optional<FeatureInitParams> mapped = imageInit.map( params->
			new FeatureInitParamsSharedObjs(params)
		);
		return mapped.orElse(
			new FeatureInitParams()	
		);
	}

	private static <T extends FeatureInput> void assertResultTolerance(
		String message,
		Feature<T> feature,
		T params,
		FeatureInitParams initParams,
		double expectedResult,
		double delta
	) throws FeatureCalcException {
		double res = FeatureTestCalculator.calcSequentialSession(
			feature,
			params,
			initParams
		);
		assertEquals(message, expectedResult, res, 1e-20);
	}
	
	private static <T extends FeatureInput> double calcSequentialSession( Feature<T> feature, T params, FeatureInitParams initParams ) throws FeatureCalcException {
		
		FeatureCalculatorSingle<T> calculator = FeatureSession.with(
			feature,
			initParams,
			new SharedFeatureMulti(),
			LoggingFixture.suppressedLogErrorReporter()
		);
		
		return calculator.calc(params);
	}
}
