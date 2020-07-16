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
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithContainer;

import lombok.Getter;
import lombok.Setter;

/**
 * Returns only the objects that intersect with at least one object in the container
 *   
 * @author feehano
 *
 */
public class IntersectsWithContainer extends ObjectCollectionProviderWithContainer {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private boolean inverse=false;	// If set, we return the objects that DO NOT intersect
	// END BEAN PROPERTIES
	
	@Override
	public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {

		ObjectCollection objectsContainer = containerRequired();
		
		return objects.stream().filter( object->
			includeObject(object,objectsContainer)
		);
	}
	
	private boolean includeObject(ObjectMask object, ObjectCollection objectsContainer) {
		boolean intersection = doesObjectIntersect(object,objectsContainer);
		
		if (inverse) {
			return !intersection;
		} else {
			return intersection;
		}
	}
	
	private static boolean doesObjectIntersect( ObjectMask object, ObjectCollection container ) {
		return container.stream().anyMatch(object::hasIntersectingVoxels);
	}
}