package org.anchoranalysis.plugin.image.bean.obj.filter.independent;

import java.util.Optional;

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
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.obj.filter.ObjectFilterPredicate;

/**
 * Only accepts an object if it has greater (or EQUAL) intersection with objMaskProviderGreater than objMaskProviderLesser
 * 
 * <p>If an object intersects with neither, it still gets accepted, as both return 0</p>
 * 
 * @author Owen Feehan
 *
 */
public class GreaterIntersectionWith extends ObjectFilterPredicate {

	// START BEAN PROPERTIES
	@BeanField
	private ObjectCollectionProvider objsGreater;
	
	@BeanField
	private ObjectCollectionProvider objsLesser;
	// END BEAN PROPERTIES
	
	private ObjectCollection intersectionGreater;
	private ObjectCollection intersectionLesser;

	@Override
	protected void start(Optional<ImageDimensions> dim, ObjectCollection objsToFilter) throws OperationFailedException {
		super.start(dim, objsToFilter);
		try {
			intersectionGreater = objsGreater.create();
			intersectionLesser = objsLesser.create();
			
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
	}
	
	@Override
	protected boolean match(ObjectMask om, Optional<ImageDimensions> dim)
			throws OperationFailedException {

		int cntGreater =  intersectionGreater.countIntersectingPixels(om);
		int cntLesser =  intersectionLesser.countIntersectingPixels(om);
		
		return cntGreater >= cntLesser;
	}

	@Override
	protected void end() throws OperationFailedException {
		super.end();
		intersectionGreater = null;
		intersectionLesser = null;
	}

	public ObjectCollectionProvider getObjsGreater() {
		return objsGreater;
	}
	
	public void setObjsGreater(ObjectCollectionProvider objsGreater) {
		this.objsGreater = objsGreater;
	}

	public ObjectCollectionProvider getObjsLesser() {
		return objsLesser;
	}
	
	public void setObjsLesser(ObjectCollectionProvider objsLesser) {
		this.objsLesser = objsLesser;
	}
}
