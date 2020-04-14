package org.anchoranalysis.test.feature.plugins.objs;

import org.anchoranalysis.core.geometry.Point2i;

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

import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;

public class ParamsOverlappingCircleFixture {
	
	private static final int DEFAULT_CIRCLE_RADIUS = 30;
	
	private static final int DEFAULT_POS_X = 50;
	private static final int DEFAULT_POS_Y = 50;
	
	private ParamsOverlappingCircleFixture() {
		
	}
	
	/**
	 * Two object-masks of circles in different locations WITH some overlap
	 * 
	 * @param sameSize iff TRUE the object-masks are the same size, otherwise not
	 * @return the params populated with the two masks
	 */
	public static FeatureObjMaskPairParams twoOverlappingCircles( boolean sameSize ) {
		FeatureObjMaskPairParams params = new FeatureObjMaskPairParams(
			CircleObjMaskFixture.circleAt(
				new Point2i( DEFAULT_POS_X, DEFAULT_POS_Y ),
				DEFAULT_CIRCLE_RADIUS
			),
			CircleObjMaskFixture.circleAt(
				new Point2i( DEFAULT_POS_X + 10, DEFAULT_POS_Y ),
				radiusMaybeExtra(sameSize, 3)
			)
		);
		params.setNrgStack( CircleObjMaskFixture.nrgStack() );
		return params;
	}
	
	/**
	 * Two object-masks of circles in different locations WITHOUT any overlap
	 * 
	 * @param sameSize iff TRUE the object-masks are the same size, otherwise not
	 * @return the params populated with the two masks
	 */
	public static FeatureObjMaskPairParams twoNonOverlappingCircles( boolean sameSize) {
		FeatureObjMaskPairParams params = new FeatureObjMaskPairParams(
			CircleObjMaskFixture.circleAt(
				new Point2i( DEFAULT_POS_X, DEFAULT_POS_Y ),
				DEFAULT_CIRCLE_RADIUS
			),
			CircleObjMaskFixture.circleAt(
				new Point2i( DEFAULT_POS_X, DEFAULT_POS_Y + (DEFAULT_CIRCLE_RADIUS*3) ),
				radiusMaybeExtra(sameSize, -3)
			)
		);
		params.setNrgStack( CircleObjMaskFixture.nrgStack() );
		return params;
	}
	
	/** If flag is true, adds extra to the default radius value */
	private static int radiusMaybeExtra( boolean flag, int extra ) {
		if (flag) {
			return DEFAULT_CIRCLE_RADIUS;
		} else {
			return DEFAULT_CIRCLE_RADIUS + extra;
		}
	}
}
