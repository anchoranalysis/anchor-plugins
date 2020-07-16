package org.anchoranalysis.plugin.image.bean.object.provider.filter;

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
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithDimensions;

import lombok.Getter;
import lombok.Setter;

/**
 * Considers all possible pairs of objects in a provider, and removes those that touch the border.
 * 
 * @author Owen Feehan
 *
 */
public class RemoveTouchingBorder extends ObjectCollectionProviderWithDimensions {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private boolean useZ = true;
	// END BEAN PROPERTIES
	
	@Override
	public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {
		
		ImageDimensions dim = createDimensions();
		
		return objects.stream().filter( objectMask ->
			!atBorder(objectMask, dim)
		);
	}
	
	private boolean atBorder(ObjectMask object, ImageDimensions dim) {
		if (useZ) {
			return object.getBoundingBox().atBorder(dim);
		} else {
			return object.getBoundingBox().atBorderXY(dim);
		}
	}
}