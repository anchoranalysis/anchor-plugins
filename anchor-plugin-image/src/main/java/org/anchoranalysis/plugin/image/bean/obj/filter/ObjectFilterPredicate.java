package org.anchoranalysis.plugin.image.bean.obj.filter;

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


import java.util.Optional;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.objmask.filter.ObjectFilter;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objectmask.ObjectMask;

import org.anchoranalysis.image.objectmask.ObjectCollection;

/**
 * Uses a predicate to make a decision whether to keep objects or not.
 * 
 * @author Owen Feehan
 *
 */
public abstract class ObjectFilterPredicate extends ObjectFilter {

	@Override
	public void filter(ObjectCollection objsToFilter, Optional<ImageDim> dim, Optional<ObjectCollection> objsRejected) throws OperationFailedException {

		if (!precondition(objsToFilter)) {
			return;
		}
		
		start(dim, objsToFilter);
		
		objsToFilter.remove(
			om -> !match(om,dim),
			objsRejected
		);
		
		end();
	}
	
	/** A precondition, which if evaluates to false, cancels the filter i.e. nothing is removed */
	protected boolean precondition(ObjectCollection objsToFilter) {
		return true;
	}
	
	protected void start(Optional<ImageDim> dim, ObjectCollection objsToFilter) throws OperationFailedException {
		// Default implementation, nothing to do
	}
	
	/** A predicate condition for an object to be kept in the collection */
	protected abstract boolean match( ObjectMask om, Optional<ImageDim> dim ) throws OperationFailedException;
	
	protected void end() throws OperationFailedException {
		// Default implementation, nothing to do
	}

}
