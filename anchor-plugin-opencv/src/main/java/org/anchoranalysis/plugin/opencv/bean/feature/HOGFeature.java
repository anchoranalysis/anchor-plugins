package org.anchoranalysis.plugin.opencv.bean.feature;

/*-
 * #%L
 * anchor-plugin-opencv
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

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.size.SizeXY;
import org.anchoranalysis.image.feature.bean.FeatureStack;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.plugin.opencv.CVInit;

/**
 * One part of a <a href="https://en.wikipedia.org/wiki/Histogram_of_oriented_gradients">Histogram of Oriented Gradients</a> descriptor, as applied to an image stack.
 * 
 * <p>See <a href="https://docs.opencv.org/3.4/d5/d33/structcv_1_1HOGDescriptor.html#a723b95b709cfd3f95cf9e616de988fc8">OpenCV HOGDescriptor documentation</a> for details of implementation.</p>
 * 
 * @author Owen Feehan
 *
 */
public class HOGFeature extends FeatureStack {
	
	static {
		CVInit.alwaysExecuteBeforeCallingLibrary();
	}
	
	// START BEAN PROPERTIES
	/** The input is rescaled to this width/height before calculating HOG descriptors */
	@BeanField @OptionalBean
	private SizeXY resizeTo;
	
	/** Parameters used for calculating HOG */
	@BeanField
	private HOGParameters params = new HOGParameters();
	
	/** Which index to return from the HOG descriptor */
	@BeanField @NonNegative
	private int index = 0;
	// END BEAN PROPRERTIES

	@Override
	public void checkMisconfigured(BeanInstanceMap defaultInstances) throws BeanMisconfiguredException {
		super.checkMisconfigured(defaultInstances);
		if (resizeTo!=null) {
			if (resizeTo.getWidth() < params.getWindowSize().getWidth()) {
				throw new BeanMisconfiguredException("The resizeTo width is smaller than the window-width");
			}
			if (resizeTo.getHeight() < params.getWindowSize().getHeight()) {
				throw new BeanMisconfiguredException("The resizeTo height is smaller than the window-height");
			}
		}
	}
	
	@Override
	protected double calc(SessionInput<FeatureInputStack> input) throws FeatureCalcException {
		float[] arr = input.calc(
			new CalculateHOGDescriptor(
				Optional.ofNullable(resizeTo),
				params
			)
		);
		
		if (index>=arr.length) {
			throw new FeatureCalcException(
				String.format("Index %d is out-of-bounds as the hog-descriptor has %d elements", index, arr.length)	
			);
		}
		
		return arr[index];
	}

	public SizeXY getResizeTo() {
		return resizeTo;
	}

	public void setResizeTo(SizeXY resizeTo) {
		this.resizeTo = resizeTo;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public HOGParameters getParams() {
		return params;
	}

	public void setParams(HOGParameters params) {
		this.params = params;
	}

	@Override
	public String getParamDscr() {
		return Integer.toString(index);
	}

}
