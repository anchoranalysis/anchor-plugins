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
import org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting.FeatureIntersectingObjects;
import org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting.MinFeatureIntersectingObjectsAboveThreshold;
import org.junit.Test;

public class MinFeatureIntersectingObjectsAboveThresholdTest {
	
	private static final FeatureIntersectingObjects FEATURE = FeatureHelper.createWithThreshold(
		new MinFeatureIntersectingObjectsAboveThreshold()
	); 
	
	/**
	 * We expect always the same number of pixels apart from the last object which has 0 neighbours
	 * 
	 * @throws FeatureCalcException
	 * @throws InitException
	 * @throws OperationFailedException
	 */
	@Test
	public void testSameSizes() throws FeatureCalcException, InitException, OperationFailedException {
		InteresectingObjectsTestHelper.testPositions(
			"sameSize",
			FEATURE,
			true,
			FeatureHelper.VALUE_NO_OBJECTS,
			FeatureHelper.VALUE_NO_OBJECTS,
			FeatureHelper.VALUE_NO_OBJECTS,
			FeatureHelper.VALUE_NO_OBJECTS
		);
	}

	/**
	 * We expect a growing number of pixels apart from the last object which has 0 neighbours
	 * 
	 * @throws FeatureCalcException
	 * @throws InitException
	 * @throws OperationFailedException
	 */
	@Test
	public void testDifferentSizes() throws FeatureCalcException, InitException, OperationFailedException {
		InteresectingObjectsTestHelper.testPositions(
			"differentSize",
			FEATURE,
			false,
			FeatureHelper.VALUE_NO_OBJECTS,
			FeatureHelper.EXPECTED_NUM_PIXELS_SECOND_CIRCLE,
			FeatureHelper.VALUE_NO_OBJECTS,
			FeatureHelper.VALUE_NO_OBJECTS
		);
	}

}
