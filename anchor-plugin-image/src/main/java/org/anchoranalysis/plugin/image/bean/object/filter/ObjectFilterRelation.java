/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.image.bean.object.filter;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.GreaterThanEqualToBean;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.math.relation.DoubleBiPredicate;

/**
 * An independent object-filter that uses a relation in its predicate.
 *
 * @author Owen Feehan
 */
public abstract class ObjectFilterRelation extends ObjectFilterPredicate {

    // START BEAN PROPERTIES
    /** The relation to be used in the filter predicate. */
    @BeanField @Getter @Setter private RelationBean relation = new GreaterThanEqualToBean();

    // END BEAN PROPERTIES

    /** The resolved relation as a {@link DoubleBiPredicate}. */
    private DoubleBiPredicate relationResolved;

    @Override
    protected boolean precondition(ObjectCollection objectsToFilter) {
        return true;
    }

    @Override
    protected void start(Optional<Dimensions> dimensions, ObjectCollection objectsToFilter)
            throws OperationFailedException {
        relationResolved = relation.create();
    }

    @Override
    protected boolean match(ObjectMask object, Optional<Dimensions> dimensions)
            throws OperationFailedException {
        return match(object, dimensions, relationResolved);
    }

    /**
     * Performs the matching operation using the resolved relation.
     *
     * @param object the {@link ObjectMask} to be evaluated
     * @param dim optional {@link Dimensions} of the object
     * @param relation the resolved {@link DoubleBiPredicate} relation
     * @return true if the object matches the criteria, false otherwise
     * @throws OperationFailedException if the matching operation fails
     */
    protected abstract boolean match(
            ObjectMask object, Optional<Dimensions> dim, DoubleBiPredicate relation)
            throws OperationFailedException;

    @Override
    protected void end() throws OperationFailedException {
        relationResolved = null;
    }
}
