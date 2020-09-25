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

package org.anchoranalysis.plugin.image.feature.object.calculation.single;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationMap;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;

@EqualsAndHashCode(callSuper = false)
public abstract class CalculateIncrementalOperationMap
        extends FeatureCalculationMap<
                ObjectMask, FeatureInputSingleObject, Integer, FeatureCalculationException> {
    private boolean do3D;

    public CalculateIncrementalOperationMap(boolean do3D) {
        super(100);
        this.do3D = do3D;
    }

    protected CalculateIncrementalOperationMap(CalculateIncrementalOperationMap other) {
        super(100);
        this.do3D = other.do3D;
    }

    @Override
    protected ObjectMask execute(FeatureInputSingleObject params, Integer key)
            throws FeatureCalculationException {
        Extent extent = params.dimensionsRequired().extent();

        if (key == 0) {
            throw new FeatureCalculationException("Key must be > 0");
        }

        int lowestExistingKey = findHighestExistingKey(key - 1);

        ObjectMask object =
                lowestExistingKey != 0 ? getOrNull(lowestExistingKey) : params.getObject();

        try {
            for (int i = (lowestExistingKey + 1); i <= key; i++) {

                ObjectMask next = applyOperation(object, extent, do3D);

                // save in cache, as long as it's not the final one, as this will save after the
                // function executes
                if (i != key) {
                    this.put(i, next);
                }

                object = next;
            }
            return object;

        } catch (OperationFailedException e) {
            throw new FeatureCalculationException(e);
        }
    }

    protected abstract ObjectMask applyOperation(ObjectMask object, Extent extent, boolean do3D)
            throws OperationFailedException;
    
    /** Lowest existing key. 0 if no key exists. */
    private int findHighestExistingKey(int max) {
        for (int i = max; i >= 1; i--) {
            if (this.hasKey(i)) {
                return i;
            }
        }
        return 0;
    }
}
