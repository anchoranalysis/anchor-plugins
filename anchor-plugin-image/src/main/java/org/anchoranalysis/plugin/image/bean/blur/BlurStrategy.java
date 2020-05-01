package org.anchoranalysis.plugin.image.bean.blur;

/*-
 * #%L
 * anchor-plugin-image
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

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.image.convert.ImageUnitConverter;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

/**
 * A method for applying blurring to an image
 * 
 * @author Owen Feehan
 *
 */
public abstract class BlurStrategy extends AnchorBean<BlurStrategy> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField @Positive
	private double sigma = 3;
	
	@BeanField
	private boolean sigmaInMeters = false;	// Treats sigma if it's microns
	// END BEAN PROPERTIES
	
	public abstract void blur( VoxelBoxWrapper voxelBox, ImageDim dim, LogReporter logger ) throws OperationFailedException;
	
	protected double calcSigma( ImageDim dim, LogReporter logger ) throws OperationFailedException {
		
		double sigmaToUse = sigma;
		
		if (sigmaInMeters) {
			// Then we reconcile our sigma in microns against the Pixel Size XY (Z is taken care of later)
			sigmaToUse = ImageUnitConverter.convertFromMeters( sigma, dim.getRes() );
			
			logger.logFormatted("Converted sigmaInMeters=%f into sigma=%f", sigma, sigmaToUse);
		} 
		
		if (sigmaToUse > dim.getX() || sigmaToUse > dim.getY()) {
			throw new OperationFailedException("The calculated sigma is FAR TOO LARGE. It is larger than the entire channel it is applied to");
		}
		
		return sigmaToUse;
	}

	public double getSigma() {
		return sigma;
	}

	public void setSigma(double sigma) {
		this.sigma = sigma;
	}

	public boolean isSigmaInMeters() {
		return sigmaInMeters;
	}

	public void setSigmaInMeters(boolean sigmaInMeters) {
		this.sigmaInMeters = sigmaInMeters;
	}
}
