package org.anchoranalysis.plugin.image.bean.object.provider.mask;



import org.anchoranalysis.bean.annotation.BeanField;

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


import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.binary.logical.BinaryChnlAnd;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;

import lombok.Getter;
import lombok.Setter;

/**
 * Reduce the size of individual objects (if neccessary, and minimally) to fit within a binary-mask.
 * <p>
 * Note the masking overwrites the current objects in situ, so it is often recommended to a duplicate
 * operation beforehand to preserve the existing objects.
 * <p.
 * This is equivalent to applying a logical AND operation on each voxel @code{binary-mask AND object-mask}. 
 * 
 * @author feehano
 *
 */
public class AndObjectsWithMasks extends ObjectCollectionProviderUnary {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private BinaryChnlProvider mask;
	// END BEAN PROPERTIES
	
	@Override
	public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {
		return createFromObjects(
			objects,
			mask.create()
		);
	}
	
	private ObjectCollection createFromObjects(ObjectCollection objects, Mask mask) {
		objects.forEach( objectMask ->
			applyMask(objectMask, mask)
		);
		return objects;
	}
	
	private static void applyMask( ObjectMask object, Mask mask ) {
		
		// Just the portion of the mask that matches the bounding box of our object
		ObjectMask maskObject = mask.region(object.getBoundingBox(), true);
		
		BinaryChnlAnd.apply(
			object.binaryVoxelBox().getVoxelBox(),
			maskObject.binaryVoxelBox().getVoxelBox(),
			object.binaryVoxelBox().getBinaryValues().createByte(),
			maskObject.binaryVoxelBox().getBinaryValues().createByte()
		);		
	}
}
