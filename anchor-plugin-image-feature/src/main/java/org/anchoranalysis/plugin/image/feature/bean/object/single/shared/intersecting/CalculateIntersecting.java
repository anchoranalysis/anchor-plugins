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

package org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.calculate.cache.part.ResolvedPart;
import org.anchoranalysis.image.feature.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.plugin.image.feature.object.calculation.delegate.CalculateInputFromDelegate;

/**
 * Calculates a {@link FeatureInputPairObjects} from a {@link FeatureInputSingleObject} and an
 * intersecting {@link ObjectCollection}.
 */
@EqualsAndHashCode(callSuper = true)
public class CalculateIntersecting
        extends CalculateInputFromDelegate<
                FeatureInputPairObjects, FeatureInputSingleObject, ObjectCollection> {

    /** The index of the object in the intersecting collection to use. */
    private int index;

    /**
     * Creates a new {@link CalculateIntersecting} instance.
     *
     * @param intersecting the resolved part containing the intersecting objects
     * @param index the index of the object in the intersecting collection to use
     */
    public CalculateIntersecting(
            ResolvedPart<ObjectCollection, FeatureInputSingleObject> intersecting, int index) {
        super(intersecting);
        this.index = index;
    }

    @Override
    protected FeatureInputPairObjects deriveFromDelegate(
            FeatureInputSingleObject input, ObjectCollection delegate) {
        return new FeatureInputPairObjects(
                input.getObject(), delegate.get(index), input.getEnergyStackOptional());
    }
}
