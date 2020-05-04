package org.anchoranalysis.bean.provider.objs.merge;

/*-
 * #%L
 * anchor-plugin-image
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

import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.shared.relation.GreaterThanBean;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.bean.operator.Constant;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPair;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.plugin.image.bean.merge.ObjMaskProviderMergePair;
import org.anchoranalysis.test.LoggingFixture;
import org.junit.Test;
import static org.anchoranalysis.bean.provider.objs.merge.MergeTestHelper.*;

public class ObjMaskProviderMergePairTest {

	static {
		RegisterBeanFactories.registerAllPackageBeanFactories();
	}

	/** Counts the number of pixels in the merge of the pair */
	public static class MockFeature extends FeatureObjMaskPair {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		protected double calc(SessionInput<FeatureInputPairObjs> input) throws FeatureCalcException {
			int pixelsObj1 = input.get().getFirst().numPixels();
			int pixelsObj2 = input.get().getSecond().numPixels();
			return Math.min(pixelsObj1, pixelsObj2);
		}
		
	}
	
	@Test
	public void testAllMerge() throws BeanMisconfiguredException, CreateException, InitException {
		
		MergeTestHelper.testProviderOn(
			EXPECTED_LINEAR_RESULT_ALL_INTERSECTING_MERGED,
			createMergePair(OBJS_LINEAR_INTERSECTING, 1)
		);
	}
	
	
	@Test
	public void testSomeMerge() throws BeanMisconfiguredException, CreateException, InitException {
		
		// Threshold chosen so the first three objects are too small to merge
		MergeTestHelper.testProviderOn(
			EXPECTED_LINEAR_RESULT_FIRST_THREE_NOT_MERGING,
			createMergePair(OBJS_LINEAR_INTERSECTING, 300)
		);
	}	
	
	
	
	private static ObjMaskProviderMergePair createMergePair( ObjMaskCollection objs, int threshold) throws CreateException {
		
		LogErrorReporter logger = LoggingFixture.simpleLogErrorReporter();
		
		ObjMaskProviderMergePair provider = new ObjMaskProviderMergePair();
		
		provider.setObjs(
			ProviderFixture.providerFor(objs)
		);
		provider.setFeatureEvaluatorThreshold(
			FeatureEvaluatorFixture.createNrg( new Constant<>(threshold), logger )
		);
		provider.setFeatureEvaluatorMerge(
			FeatureEvaluatorFixture.createNrg( new MockFeature(), logger )
		);
		provider.setRelation(
			new GreaterThanBean()
		);
		return provider;
	}
}
