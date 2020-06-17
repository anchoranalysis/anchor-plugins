package ch.ethz.biol.cell.imageprocessing.objmask.provider;

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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.interpolator.InterpolatorFactory;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.scale.ScaleFactor;

public class ObjMaskProviderScale extends ObjMaskProviderDimensions {

	// START BEAN PROPERTIES
	@BeanField
	private ScaleCalculator scaleCalculator;
	// END BEAN PROPERTIES
	
	@Override
	public ObjectCollection createFromObjs(ObjectCollection objsCollection) throws CreateException {
		
		ImageDim dim = createDim();
		
		ScaleFactor sf;
		try {
			sf = scaleCalculator.calc(dim);
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}

		return objsCollection.scale(
			sf,
			InterpolatorFactory.getInstance().binaryResizing()
		);
		
	}
	
	public ScaleCalculator getScaleCalculator() {
		return scaleCalculator;
	}

	public void setScaleCalculator(ScaleCalculator scaleCalculator) {
		this.scaleCalculator = scaleCalculator;
	}
}
