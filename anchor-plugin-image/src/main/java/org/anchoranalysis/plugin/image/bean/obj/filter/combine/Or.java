package org.anchoranalysis.plugin.image.bean.obj.filter.combine;

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


import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.objmask.filter.ObjectFilter;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;

/**
 * Applies multiples filter with logical OR i.e. an object must pass any one of the filter steps to remain.
 * 
 * @author Owen Feehan
 *
 */
public class Or extends ObjectFilterCombine {

	@Override
	public ObjectCollection filter(
		ObjectCollection objsToFilter,
		Optional<ImageDim> dim,
		Optional<ObjectCollection> objsRejected
	) throws OperationFailedException {

		// Stores any successful items in a set
		Set<ObjectMask> setAccepted = findAcceptedObjs(objsToFilter, dim);
		
		// Adds the rejected-objects
		objsRejected.ifPresent( rejected-> 
			rejected.addAll(
				determineRejected(objsToFilter, setAccepted)		
			)
		);

		// Creates the accepted-objects
		return new ObjectCollection( setAccepted.stream() );
	}
	
	/** Finds the accepted objects (i.e. objects that pass any one of the filters) */
	private Set<ObjectMask> findAcceptedObjs(ObjectCollection objsToFilter, Optional<ImageDim> dim) throws OperationFailedException {
		Set<ObjectMask> setAccepted = new HashSet<ObjectMask>();
		
		for (ObjectFilter indFilter : getList()) {
			setAccepted.addAll(
				indFilter.filter(objsToFilter, dim, Optional.empty()).asList()
			);
		}
		
		return setAccepted;
	}
	
	/** Determines which objects are rejected */
	private static ObjectCollection determineRejected(ObjectCollection objsToFilter, Set<ObjectMask> setAccepted) {
		return objsToFilter.filter( obj ->
			!setAccepted.contains(obj)
		);
	}
}
