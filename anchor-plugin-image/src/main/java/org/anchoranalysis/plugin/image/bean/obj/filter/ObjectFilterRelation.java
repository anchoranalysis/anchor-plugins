package org.anchoranalysis.plugin.image.bean.obj.filter;

import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.GreaterThanEqualToBean;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.objectmask.ObjectMask;

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
	protected void start(Optional<ImageDim> dim, ObjectCollection objsToFilter) throws OperationFailedException {
		relationResolved = relation.create();
	}
	
	@Override
	protected boolean match(ObjectMask om, Optional<ImageDim> dim) throws OperationFailedException {
		return match(om, dim, relationResolved);
	}
	
	protected abstract boolean match(ObjectMask om, Optional<ImageDim> dim, RelationToValue relation) throws OperationFailedException;

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
