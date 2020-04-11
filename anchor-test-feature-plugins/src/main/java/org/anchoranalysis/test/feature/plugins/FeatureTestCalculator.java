package org.anchoranalysis.test.feature.plugins;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.store.SharedObjects;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.ResultsVector;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.session.SequentialSession;
import org.anchoranalysis.feature.session.SessionFactory;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingleFromMulti;
import org.anchoranalysis.feature.shared.SharedFeatureSet;
import org.anchoranalysis.image.feature.init.FeatureInitParamsImageInit;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.test.LoggingFixtures;


public class FeatureTestCalculator {

	private FeatureTestCalculator() {
		
	}
	
	public static <T extends FeatureCalcParams> void assertDoubleResult( String message, Feature<T> feature, T params, double expectedResult ) throws FeatureCalcException, InitException {
		assertDoubleResult(message, feature, params, Optional.empty(), expectedResult);
	}
	
	public static <T extends FeatureCalcParams> void assertIntResult( String message, Feature<T> feature, T params, int expectedResult ) throws FeatureCalcException, InitException {
		assertIntResult(message, feature, params, Optional.empty(), expectedResult);
	}
	
	public static <T extends FeatureCalcParams> void assertDoubleResult( String message, Feature<T> feature, T params, Optional<ImageInitParams> imageInit, double expectedResult ) throws FeatureCalcException {
		assertResultTolerance(
			message,
			feature,
			params,
			createInitParams(imageInit),
			expectedResult,
			1e-4
		);
	}
	
	public static <T extends FeatureCalcParams> void assertIntResult( String message, Feature<T> feature, T params, Optional<ImageInitParams> imageInit, int expectedResult ) throws FeatureCalcException {
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
			new FeatureInitParamsImageInit(params)
		);
		return mapped.orElse(
			new FeatureInitParams()	
		);
	}

	private static <T extends FeatureCalcParams> void assertResultTolerance(
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
	
	private static <T extends FeatureCalcParams> double calcSequentialSession( Feature<T> feature, T params, FeatureInitParams initParams ) throws FeatureCalcException {
		
		FeatureCalculatorSingle<T> calculator = SessionFactory.createAndStart(
			feature,
			initParams,
			new SharedFeatureSet<>(),
			LoggingFixtures.simpleLogErrorReporter()
		);
		
		return calculator.calcOne(params);
	}
}
