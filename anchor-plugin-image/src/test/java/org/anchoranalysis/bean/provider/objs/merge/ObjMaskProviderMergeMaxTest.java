package org.anchoranalysis.bean.provider.objs.merge;

import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.plugin.image.bean.merge.ObjMaskProviderMergeMax;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.feature.plugins.MockFeatureWithCalculationFixture.MockFeatureWithCalculation;
import org.junit.Test;

public class ObjMaskProviderMergeMaxTest {

	static {
		RegisterBeanFactories.registerAllPackageBeanFactories();
	}
	
	@Test
	public void test() throws BeanMisconfiguredException, CreateException, InitException {
		
		MergeTestHelper.testProviderOn(
			MergeTestHelper.EXPECTED_LINEAR_RESULT_ALL_INTERSECTING_MERGED,
			createMergeMax(MergeTestHelper.OBJS_LINEAR_INTERSECTING, 1)
		);
	}
	
	private static ObjMaskProviderMergeMax createMergeMax( ObjMaskCollection objs, int threshold ) throws CreateException {
		
		LogErrorReporter logger = LoggingFixture.simpleLogErrorReporter();
		
		MockFeatureWithCalculation feature = new MockFeatureWithCalculation();
		
		ObjMaskProviderMergeMax provider = new ObjMaskProviderMergeMax();
		
		provider.setObjs(
			ProviderFixture.providerFor(objs)
		);
		provider.setFeatureEvaluator(
			FeatureEvaluatorFixture.createNrg(feature, logger)
		);
		
		return provider;
	}
}
