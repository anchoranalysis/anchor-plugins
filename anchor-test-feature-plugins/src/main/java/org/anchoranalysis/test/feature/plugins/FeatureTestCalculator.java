package org.anchoranalysis.test.feature.plugins;

import static org.junit.Assert.assertEquals;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.ResultsVector;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.session.SequentialSession;
import org.anchoranalysis.feature.shared.SharedFeatureSet;
import org.anchoranalysis.test.LoggingFixtures;


public class FeatureTestCalculator {

	private FeatureTestCalculator() {
		
	}
	
	public static <T extends FeatureCalcParams> void assertDoubleResult( String message, Feature<T> feature, T params, double expectedResult ) throws FeatureCalcException, InitException {
		assertResultTolerance(message, feature, params, expectedResult, 1e-4);
	}
	
	public static <T extends FeatureCalcParams> void assertIntResult( String message, Feature<T> feature, T params, int expectedResult ) throws FeatureCalcException, InitException {
		assertResultTolerance(message, feature, params, expectedResult, 1e-20);
	}

	private static <T extends FeatureCalcParams> void assertResultTolerance( String message, Feature<T> feature, T params, double expectedResult, double delta ) throws FeatureCalcException, InitException {
		double res = FeatureTestCalculator.calcSequentialSession(feature, params);
		assertEquals(message, expectedResult, res, 1e-20);
	}
	
	private static <T extends FeatureCalcParams> double calcSequentialSession( Feature<T> feature, T params ) throws FeatureCalcException, InitException {
		SequentialSession<T> session = new SequentialSession<>(feature);
		session.start( new FeatureInitParams(), new SharedFeatureSet<>(), LoggingFixtures.simpleLogErrorReporter() );
		ResultsVector rv = session.calc(params);
		return rv.get(0);
	}
}
