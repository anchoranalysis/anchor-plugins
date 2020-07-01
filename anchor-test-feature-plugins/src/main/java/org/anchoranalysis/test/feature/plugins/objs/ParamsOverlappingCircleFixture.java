package org.anchoranalysis.test.feature.plugins.objs;

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

import java.util.Optional;

import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;

public class ParamsOverlappingCircleFixture {
	
	private static final int DEFAULT_CIRCLE_RADIUS = 30;
	
	private static final int DEFAULT_POS_X = 50;
	private static final int DEFAULT_POS_Y = 50;
	
	private ParamsOverlappingCircleFixture() {}
	
	/**
	 * Two object-masks of circles in different locations WITH some overlap
	 * 
	 * @param sameSize iff TRUE the object-masks are the same size, otherwise not
	 * @return the params populated with the two masks
	 */
	public static FeatureInputPairObjects twoOverlappingCircles( boolean sameSize ) {
		return twoCircles(
			10,
			0,
			sameSize,			
			3
		);
	}
	
	/**
	 * Two object-masks of circles in different locations WITHOUT any overlap
	 * 
	 * @param sameSize iff TRUE the object-masks are the same size, otherwise not
	 * @return the params populated with the two masks
	 */
	public static FeatureInputPairObjects twoNonOverlappingCircles( boolean sameSize) {
		return twoCircles(
			0,
			(DEFAULT_CIRCLE_RADIUS*3),
			sameSize,
			-3
		);
	}
	
	
	private static FeatureInputPairObjects twoCircles(
		int shiftPositionX,
		int shiftPositionY,
		boolean sameSize,
		int extraRadius
	) {
		return new FeatureInputPairObjects(
			CircleObjMaskFixture.circleAt(
				position(0,0),
				DEFAULT_CIRCLE_RADIUS
			),
			CircleObjMaskFixture.circleAt(
				position(shiftPositionX,shiftPositionY),
				radiusMaybeExtra(sameSize, extraRadius)
			),
			Optional.of(
				CircleObjMaskFixture.nrgStack()
			)
		);
	}
	
	private static Point2i position( int shiftPositionX, int shiftPositionY ) {
		return new Point2i(
			DEFAULT_POS_X + shiftPositionX,
			DEFAULT_POS_Y + shiftPositionY
		);
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
