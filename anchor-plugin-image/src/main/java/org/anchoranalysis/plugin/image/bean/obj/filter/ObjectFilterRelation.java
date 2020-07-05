package org.anchoranalysis.plugin.image.bean.obj.filter;

/*-
 * #%L
 * anchor-plugin-image
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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.GreaterThanEqualToBean;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * An independent object-filter that uses a relation in its predicate.
 * 
 * @author Owen Feehan
 *
 */
public abstract class ObjectFilterRelation extends ObjectFilterPredicate {

	// START BEAN PROPERTIES
	@BeanField
	private RelationBean relation = new GreaterThanEqualToBean();
	// END BEAN PROPERTIES
	
	private RelationToValue relationResolved;
	
	@Override
	protected void start(Optional<ImageDimensions> dim, ObjectCollection objsToFilter) throws OperationFailedException {
		relationResolved = relation.create();
	}
	
	@Override
	protected boolean match(ObjectMask om, Optional<ImageDimensions> dim) throws OperationFailedException {
		return match(om, dim, relationResolved);
	}
	
	protected abstract boolean match(ObjectMask om, Optional<ImageDimensions> dim, RelationToValue relation) throws OperationFailedException;

	@Override
	protected void end() throws OperationFailedException {
		relationResolved = null;
	}
	
	public RelationBean getRelation() {
		return relation;
	}

	public void setRelation(RelationBean relation) {
		this.relation = relation;
	}
}
