package org.anchoranalysis.plugin.image.bean.obj.merge;

import java.util.function.Function;

import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.plugin.image.test.ProviderFixture;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.feature.plugins.mockfeature.MockFeatureWithCalculationFixture;
import org.junit.Test;

public class ObjMaskProviderMergeMaxTest {
	
	static {
		RegisterBeanFactories.registerAllPackageBeanFactories();
	}

	/** This test will merge so long as the total number of pixels increases (so every possible neighbor will eventually merge!) 
	 * @throws OperationFailedException 
	 * @throws CreateException */
	@Test
	public void testMaximalNumPixels() throws BeanMisconfiguredException, InitException, OperationFailedException, CreateException {
		testLinear(
			MergeTestHelper.EXPECTED_RESULT_ALL_INTERSECTING_MERGED,
			24,
			MockFeatureWithCalculationFixture.DEFAULT_FUNC_NUM_PIXELS
		);
	}
	
	/** 
	 * This tests will merge if it brings the total number of pixels closer to 500, but not merge otherwise.
	 * 
	 * <p>Therefore some neighboring objects will merge and others will not</p>
	 * @throws OperationFailedException 
	 * @throws CreateException 
	 */
	@Test
	public void testConvergeNumPixels() throws BeanMisconfiguredException, InitException, OperationFailedException, CreateException {
		testLinear(
			8,
			23,
			ObjMaskProviderMergeMaxTest::convergeTo900
		);
	}
	
	private void testLinear( int expectedCntMerged, int expectedFeatureCalcCount, Function<FeatureInputSingleObj,Double> calculationFunc ) throws OperationFailedException, CreateException {
		MergeTestHelper.testProviderOn(
			expectedCntMerged,
			expectedFeatureCalcCount,
			expectedFeatureCalcCount,	// Same as the feature-count as it should always be a new unique object
			createMergeMax(MergeTestHelper.OBJS_LINEAR_INTERSECTING, calculationFunc)
		);
	}
	
	private static ObjMaskProviderMergeMax createMergeMax( ObjMaskCollection objs, Function<FeatureInputSingleObj,Double> calculationFunc ) throws CreateException {
		
		LogErrorReporter logger = LoggingFixture.simpleLogErrorReporter();
		
		ObjMaskProviderMergeMax provider = new ObjMaskProviderMergeMax();
		
		provider.setObjs(
			ProviderFixture.providerFor(objs)
		);
		provider.setFeatureEvaluator(
			FeatureEvaluatorFixture.createNrg(
				MockFeatureWithCalculationFixture.createMockFeatureWithCalculation(calculationFunc),
				logger
			)
		);
		
		return provider;
	}
	
	/** Merges if the number-of-pixels becomes closer to 900 */
	private static Double convergeTo900( FeatureInputSingleObj input ) {
		int diff = 900 - input.getObjMask().numPixels();
		return (double) -1 * Math.abs(diff);
	}
}
