package org.anchoranalysis.plugin.image.bean.scale;

/*
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.scale.ScaleFactorUtilities;

public class ScaleCalculatorRelativeDimensions extends ScaleCalculator {

	// START BEAN PROPERTIES
	@BeanField @OptionalBean
	private ImageDimProvider dimSource;
	
	@BeanField
	private ImageDimProvider dimTarget;
	// END BEAN PROPERTIES

	@Override
	public ScaleFactor calc(ImageDimensions srcDim)
			throws OperationFailedException {
		
		ImageDimensions sdSource = srcDim;
		if (dimSource!=null) {
			try {
				sdSource = dimSource.create();
			} catch (CreateException e) {
				throw new OperationFailedException(e);
			}
		}
		
		if (sdSource==null) {
			throw new OperationFailedException("No source dimensions can be found");
		}
		
		try {
			return ScaleFactorUtilities.calcRelativeScale(
				sdSource.getExtnt(),
				dimTarget.create().getExtnt()
			);
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
	}

	public ImageDimProvider getDimSource() {
		return dimSource;
	}

	public void setDimSource(ImageDimProvider dimSource) {
		this.dimSource = dimSource;
	}

	public ImageDimProvider getDimTarget() {
		return dimTarget;
	}

	public void setDimTarget(ImageDimProvider dimTarget) {
		this.dimTarget = dimTarget;
	}
}
