package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

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

import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.test.feature.plugins.objects.IntersectingCircleObjectsFixture;

class MultiInputFixture {

	public static final String OBJECTS_NAME = "objectsTest";
	
	public static final int NUMBER_INTERSECTING_OBJECTS = 4;
	public static final int NUMBER_NOT_INTERSECTING_OBJECTS = 2;
	
	/** A number of unique pairs of intersecting objects
	 *  (known from the output of {@link IntersectingCircleObjectsFixture.generateIntersectingObjects} with the parameterization above.
	 */
	public static final int NUMBER_PAIRS_INTERSECTING = 3;
	
	/**
	 * This creates a MultiInput with an object-collection {@code OBJECTS_NAME}
	 * 
	 * <p>It contains 6 unique objects, 4 of whom intersect, and 2 who don't intersect at all.</p>
	 * <p>Among the four who intersect, there are 3 intersections.</p>
	 * 
	 * <p>See the constants in the fixture to represent these numbers</p>.
	 * 
	 * @param nrgStack
	 * @return
	 */
	public static MultiInput createInput( NRGStack nrgStack) {
		MultiInput input = new MultiInput(
			"input",
			new StackAsProviderFixture(
				nrgStack.asStack(),
				"someName"
			)
		);
		input.objects().add(
			OBJECTS_NAME,
			() -> IntersectingCircleObjectsFixture.generateIntersectingObjects(
				NUMBER_INTERSECTING_OBJECTS,
				NUMBER_NOT_INTERSECTING_OBJECTS,
				false
			)
		);
		return input;
	}
}
