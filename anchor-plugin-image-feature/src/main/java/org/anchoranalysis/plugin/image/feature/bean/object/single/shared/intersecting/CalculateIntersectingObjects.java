/*-
 * #%L
 * anchor-plugin-image-feature
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
/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.index.ObjectCollectionRTree;
import org.anchoranalysis.image.object.ObjectCollection;

/**
 * Calculates the intersecting set of objects from a particular collection (represted by an id) and
 * the object-mask in the params
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateIntersectingObjects
        extends FeatureCalculation<ObjectCollection, FeatureInputSingleObject> {

    /**
     * A unique ID that maps 1 to 1 to {@code searchObjects} (and is therefore sufficient to
     * uniquely @{code hashCode()})
     */
    private final String id;

    /** The objects corresponding to id */
    private final ObjectCollection searchObjects;

    @Override
    protected ObjectCollection execute(FeatureInputSingleObject params) {

        ObjectCollectionRTree bboxRTree = new ObjectCollectionRTree(searchObjects);
        return bboxRTree.intersectsWith(params.getObject());
    }
}
