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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.size.SizeXY;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.MatConverter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;

/**
 * Calculates the entire HOG descriptor for an image
 * 
 * @author Owen Feehan
 *
 */
class CalculateHOGDescriptor extends FeatureCalculation<float[], FeatureInputStack> {
	
	private Optional<SizeXY> resizeTo;
	private HOGParameters params;

	/**
	 * Constructor
	 * 
	 * @param resizeTo optionally resizes the image before calculating the descriptor (useful for achiving constant-sized descriptors for different sized images)
	 * @param params parameters for the HOG-calculation
	 */
	public CalculateHOGDescriptor(Optional<SizeXY> resizeTo, HOGParameters params) {
		super();
		this.resizeTo = resizeTo;
		this.params = params;
	}

	@Override
	protected float[] execute(FeatureInputStack input) throws FeatureCalcException {
		try {
			Stack stack = extractStack(input);
			Extent extent = stack.getDimensions().getExtent();
			
			checkSize(extent);
			
			Mat img = MatConverter.makeRGBStack(stack);
			
			MatOfFloat descriptorValues = new MatOfFloat();
			params.createDescriptor(extent).compute(img, descriptorValues);

			return convertToArray(descriptorValues);
			
		} catch (CreateException | OperationFailedException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	/** Extracts a stack (that is maybe resized) 
	 * @throws FeatureCalcException 
	 * @throws OperationFailedException */
	private Stack extractStack(FeatureInputStack input) throws OperationFailedException {
		
		// We can rely that an NRG stack always exists
		Stack stack = input.getNrgStackOptional().get().getNrgStack().asStack();

		if (resizeTo.isPresent()) {
			SizeXY size = resizeTo.get(); 
			return stack.mapChnl( chnl->
				chnl.resizeXY(size.getWidth(), size.getHeight())
			);
		} else {
			return stack;
		}
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculateHOGDescriptor){
	        final CalculateHOGDescriptor other = (CalculateHOGDescriptor) obj;
	        return new EqualsBuilder()
	            .append(resizeTo, other.resizeTo)
	            .append(params, other.params)
	            .isEquals();
	    } else{
	        return false;
	    }
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(resizeTo)
			.append(params)
			.toHashCode();
	}
	
	private void checkSize(Extent extent) throws FeatureCalcException {
		if (extent.getZ()>1) {
			throw new FeatureCalcException("The image is 3D, but the feture only supports 2D images");
		}
		params.checkSize(extent);
	}
		
	private static float[] convertToArray(MatOfFloat mat) {
		float[] arr = new float[mat.rows()];
		mat.get(0, 0, arr);
		return arr;
	}
}
