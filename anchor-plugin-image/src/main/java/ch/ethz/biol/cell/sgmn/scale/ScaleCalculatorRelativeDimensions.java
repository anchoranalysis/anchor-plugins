package ch.ethz.biol.cell.sgmn.scale;

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
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.scale.ScaleFactorUtilities;

public class ScaleCalculatorRelativeDimensions extends ScaleCalculator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField @Optional
	private ImageDimProvider dimProviderSource;
	
	@BeanField
	private ImageDimProvider dimProviderTarget;
	// END BEAN PROPERTIES

	@Override
	public ScaleFactor calc(ImageDim srcDim)
			throws OperationFailedException {
		
		ImageDim sdSource = srcDim;
		if (dimProviderSource!=null) {
			try {
				sdSource = dimProviderSource.create();
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
				dimProviderTarget.create().getExtnt()
			);
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
	}

	public ImageDimProvider getDimProviderSource() {
		return dimProviderSource;
	}

	public void setDimProviderSource(ImageDimProvider dimProviderSource) {
		this.dimProviderSource = dimProviderSource;
	}

	public ImageDimProvider getDimProviderTarget() {
		return dimProviderTarget;
	}

	public void setDimProviderTarget(ImageDimProvider dimProviderTarget) {
		this.dimProviderTarget = dimProviderTarget;
	}

}
