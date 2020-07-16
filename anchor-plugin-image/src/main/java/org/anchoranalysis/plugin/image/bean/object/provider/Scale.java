package org.anchoranalysis.plugin.image.bean.object.provider;

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
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.interpolator.InterpolatorFactory;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.scale.ScaleFactor;

import lombok.Getter;
import lombok.Setter;

/**
 * Scales all the objects in the collection by a particular scale-factor.
 * 
 * @author Owen Feehan
 *
 */
public class Scale extends ObjectCollectionProviderWithDimensions {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private ScaleCalculator scaleCalculator;
	// END BEAN PROPERTIES
	
	@Override
	public ObjectCollection createFromObjects(ObjectCollection objectCollection) throws CreateException {
		
		ImageDimensions dim = createDimensions();
		
		ScaleFactor scaleFactor;
		try {
			scaleFactor = scaleCalculator.calc(dim);
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}

		return objectCollection.scale(
			scaleFactor,
			InterpolatorFactory.getInstance().binaryResizing()
		);
		
	}
}