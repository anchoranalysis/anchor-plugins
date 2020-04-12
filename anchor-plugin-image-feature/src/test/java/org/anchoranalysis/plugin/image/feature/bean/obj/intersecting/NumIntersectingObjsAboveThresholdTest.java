package org.anchoranalysis.plugin.image.feature.bean.obj.intersecting;





/*-
 * #%L
 * anchor-plugin-image-feature
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

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.junit.Test;

public class NumIntersectingObjsAboveThresholdTest {
		
	private static final int EXPECTED_REJECTED_UNDER_THRESHOLD = 0;
	
	private static final FeatureIntersectingObjs FEATURE = FeatureHelper.createWithThreshold(
		new NumIntersectingObjsAboveThreshold()
	); 
	
	@Test
	public void testSameSizes() throws FeatureCalcException, InitException, OperationFailedException {
		InteresectingObjsTestHelper.testPositions(
			"sameSize",
			FEATURE,
			true,
			EXPECTED_REJECTED_UNDER_THRESHOLD,
			EXPECTED_REJECTED_UNDER_THRESHOLD,
			EXPECTED_REJECTED_UNDER_THRESHOLD,
			NumIntersectingObjsTest.EXPECTED_LAST
		);
	}
	
	@Test
	public void testDifferentSizes() throws FeatureCalcException, InitException, OperationFailedException {
		InteresectingObjsTestHelper.testPositions(
			"differentSize",
			FEATURE,
			false,
			EXPECTED_REJECTED_UNDER_THRESHOLD,
			NumIntersectingObjsTest.EXPECTED_SECOND,
			NumIntersectingObjsTest.EXPECTED_SECOND_LAST,
			NumIntersectingObjsTest.EXPECTED_LAST
		);
	}

}

