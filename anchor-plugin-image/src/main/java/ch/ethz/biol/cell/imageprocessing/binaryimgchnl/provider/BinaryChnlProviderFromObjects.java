package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

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
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ops.BinaryChnlFromObjects;

import lombok.Getter;
import lombok.Setter;

/** 
 * Creates a BinaryImgChannel from a collection of object masks
 **/
public class BinaryChnlProviderFromObjects extends BinaryChnlProviderDimSource {

	// START BEAN
	@BeanField @Getter @Setter
	private ObjectCollectionProvider objects;
	
	@BeanField @Getter @Setter
	private boolean invert = false;
	// END BEAN

	@Override
	protected Mask createFromSource(ImageDimensions dimSource) throws CreateException {
		return create(
			objects.create(),
			dimSource,
			invert
		);
	}
	
	private static Mask create(ObjectCollection objects, ImageDimensions dim, boolean invert ) {

		BinaryValues bv = BinaryValues.getDefault();
		
		Mask maskedImage = createChannelFromObjectsMultiplex(objects, dim, bv, invert);
		return new Mask(maskedImage.getChannel(), bv);	
	}
	
	private static Mask createChannelFromObjectsMultiplex(
		ObjectCollection objects,
		ImageDimensions sd,
		BinaryValues outVal,
		boolean invert
	) {
		if (invert) {
			return BinaryChnlFromObjects.createFromNotObjects(objects, sd, outVal);
		} else {
			return BinaryChnlFromObjects.createFromObjects(objects, sd, outVal);
		}
	}
}