/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.filter;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
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
 */
public abstract class ObjectFilterRelation extends ObjectFilterPredicate {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private RelationBean relation = new GreaterThanEqualToBean();
    // END BEAN PROPERTIES

    private RelationToValue relationResolved;

    @Override
    protected void start(Optional<ImageDimensions> dim, ObjectCollection objectsToFilter)
            throws OperationFailedException {
        relationResolved = relation.create();
    }

    @Override
    protected boolean match(ObjectMask object, Optional<ImageDimensions> dim)
            throws OperationFailedException {
        return match(object, dim, relationResolved);
    }

    protected abstract boolean match(
            ObjectMask object, Optional<ImageDimensions> dim, RelationToValue relation)
            throws OperationFailedException;

    @Override
    protected void end() throws OperationFailedException {
        relationResolved = null;
    }
}
