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
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.part.CalculationPartMap;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.Extent;

/** Calculates incremental operations on {@link ObjectMask}s and stores the results in a map. */
@EqualsAndHashCode(callSuper = false)
public abstract class CalculateIncrementalOperationMap
        extends CalculationPartMap<
                ObjectMask, FeatureInputSingleObject, Integer, FeatureCalculationException> {

    /** Flag indicating whether to perform 3D operations. */
    private boolean do3D;

    /**
     * Creates a new instance with the specified 3D flag.
     *
     * @param do3D whether to perform 3D operations
     */
    protected CalculateIncrementalOperationMap(boolean do3D) {
        super(100);
        this.do3D = do3D;
    }

    /**
     * Copy constructor.
     *
     * @param other the {@link CalculateIncrementalOperationMap} to copy from
     */
    protected CalculateIncrementalOperationMap(CalculateIncrementalOperationMap other) {
        super(100);
        this.do3D = other.do3D;
    }

    @Override
    protected ObjectMask execute(FeatureInputSingleObject input, Integer key)
            throws FeatureCalculationException {
        Extent extent = input.dimensionsRequired().extent();

        if (key == 0) {
            throw new FeatureCalculationException("Key must be > 0");
        }

        int lowestExistingKey = findHighestExistingKey(key - 1);

        ObjectMask object =
                lowestExistingKey != 0 ? getOrNull(lowestExistingKey) : input.getObject();

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

    /**
     * Applies the incremental operation to the input object.
     *
     * @param object the input {@link ObjectMask}
     * @param extent the {@link Extent} of the object
     * @param do3D whether to perform 3D operations
     * @return the resulting {@link ObjectMask} after applying the operation
     * @throws OperationFailedException if the operation fails
     */
    protected abstract ObjectMask applyOperation(ObjectMask object, Extent extent, boolean do3D)
            throws OperationFailedException;

    /**
     * Finds the highest existing key that is less than or equal to the given maximum.
     *
     * @param max the maximum key to search for
     * @return the highest existing key, or 0 if no key exists
     */
    private int findHighestExistingKey(int max) {
        for (int i = max; i >= 1; i--) {
            if (this.hasKey(i)) {
                return i;
            }
        }
        return 0;
    }
}
