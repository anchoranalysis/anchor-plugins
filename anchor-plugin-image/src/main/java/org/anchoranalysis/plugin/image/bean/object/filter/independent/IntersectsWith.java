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

package org.anchoranalysis.plugin.image.bean.object.filter.independent;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.filter.ObjectFilterPredicate;

/**
 * Keeps objects which intersects with ANY ONE of a collection of other objects.
 *
 * @author Owen Feehan
 */
public class IntersectsWith extends ObjectFilterPredicate {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;
    // END BEAN PROPERTIES

    private ObjectCollection intersectWithAnyOneObject;

    @Override
    protected boolean precondition(ObjectCollection objectsToFilter) {
        return true;
    }
    
    @Override
    protected void start(Optional<ImageDimensions> dim, ObjectCollection objectsToFilter)
            throws OperationFailedException {
        super.start(dim, objectsToFilter);
        try {
            intersectWithAnyOneObject = objects.create();
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    protected boolean match(ObjectMask objectToIntersectWith, Optional<ImageDimensions> dim)
            throws OperationFailedException {
        return intersectWithAnyOneObject.stream()
                .anyMatch(objectMask -> objectMask.hasIntersectingVoxels(objectToIntersectWith));
    }

    @Override
    protected void end() throws OperationFailedException {
        intersectWithAnyOneObject = null;
    }
}
