package org.anchoranalysis.bean.provider.objs.merge;

import java.util.function.Function;

import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.plugin.image.bean.merge.ObjMaskProviderMergeMax;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.feature.plugins.MockFeatureWithCalculationFixture;
import org.junit.Test;

public class ObjMaskProviderMergeMaxTest {
	
	static {
		RegisterBeanFactories.registerAllPackageBeanFactories();
	}

	/** This test will merge so long as the total number of pixels increases (so every possible neighbor will eventually merge!) */
	@Test
	public void testMaximalNumPixels() throws BeanMisconfiguredException, CreateException, InitException {
		testLinear(
			MergeTestHelper.EXPECTED_LINEAR_RESULT_ALL_INTERSECTING_MERGED,
			MockFeatureWithCalculationFixture.DEFAULT_FUNC_NUM_PIXELS
		);
	}
	
	/** This tests will merge if it brings the total number of pixels closer to 500, but not merge otherwise.
	 * 
	 * <p>Therefore some neighboring objects will merge and others will not</p>
	 */
	@Test
	public void testNumPixelsTarget() throws BeanMisconfiguredException, CreateException, InitException {
		testLinear(
			8,
			ObjMaskProviderMergeMaxTest::convergeTo900
		);
	}
	
	private void testLinear( int expectedCntMerged, Function<FeatureInputSingleObj,Double> calculationFunc ) throws CreateException {
		MergeTestHelper.testProviderOn(
			expectedCntMerged,
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
