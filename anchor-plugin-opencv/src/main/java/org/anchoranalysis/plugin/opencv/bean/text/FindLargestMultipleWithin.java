package org.anchoranalysis.plugin.opencv.bean.text;

/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.scale.ScaleFactorUtilities;

/**
 * Finds largest multiple of an Extent without being larger than another extent
 * 
 * @author owen
 *
 */
class FindLargestMultipleWithin {

	private FindLargestMultipleWithin() {
		// NOTHING TO DO
	}

	/**
	 * Scales an extent as much as possible in BOTH dimensions without growing larger than another extent
	 * 
	 * <p>Only integral scale-factors are considered e.g. twice, thrice, 4 times etc.</p>
	 * <p>The X dimension and Y dimension are treated in unison i.e. both are scaled together</p>
	 * 
	 * @param small the extent to scale
	 * @param stayWithin a maximum size not to scale beyond
	 * @return the scaled-extent to use
	 * @throws OperationFailedException 
	 */
	public static Extent apply( Extent small, Extent stayWithin ) throws OperationFailedException {

		if (small.getX() > stayWithin.getX()) {
			throw new OperationFailedException("The extent of small in the X direction is already larger than stayWithin. This is not allowed");
		}
		
		if (small.getY() > stayWithin.getY()) {
			throw new OperationFailedException("The extent of small in the Y direction is already larger than stayWithin. This is not allowed");
		}
		
		// Non-integral scale factors
		ScaleFactor sf = ScaleFactorUtilities.calcRelativeScale(small, stayWithin);
		
		int minFactor = (int) Math.floor( Math.min(sf.getX(), sf.getY()) ); 
		
		// The integral floor of each
		ScaleFactorInt sfInt = new ScaleFactorInt(
			minFactor,
			minFactor
		);

		// The scaled extent
		Extent extentScaled = new Extent(
			sfInt.scaledX(small.getX()),
			sfInt.scaledY(small.getY()),
			1
		);
		
		return extentScaled;
	}
}
