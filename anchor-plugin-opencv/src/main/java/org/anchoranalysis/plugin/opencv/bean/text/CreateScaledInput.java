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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.scale.ScaleFactorUtilities;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.MatConverter;
import org.apache.commons.math3.util.Pair;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


/**
 * Creates a scaled-version of a stack to use as input
 * 
 * @author owen
 *
 */
class CreateScaledInput {

	/** Returns a scaled-down version of the stack, and a scale-factor that would return it to original size */
	public static Pair<Mat,ScaleFactor> apply( Stack stack, Extent targetExtent ) throws CreateException {
		
		Mat original = MatConverter.makeRGBStack(stack);
		
		Mat input = resizeMatToTarget(original, targetExtent);
		
		ScaleFactor sf = calcRelativeScale(original, input);
		
		return new Pair<>( input, sf );
	}
	
	private static ScaleFactor calcRelativeScale(Mat original, Mat resized) {
		return ScaleFactorUtilities.calcRelativeScale(
			extentFromMat(resized),
			extentFromMat(original)
		);
	}
	
	private static Extent extentFromMat( Mat mat ) {
		return new Extent(
			mat.cols(),
			mat.rows(),
			0
		);
	}
	
	private static Mat resizeMatToTarget( Mat src, Extent targetExtent ) {
		Mat dst = new Mat();
		Size sz = new Size(targetExtent.getX(),targetExtent.getY());
		Imgproc.resize(src, dst, sz);
		return dst;
	}
}
