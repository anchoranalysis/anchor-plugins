/*-
 * #%L
 * anchor-image-feature
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

package org.anchoranalysis.plugin.image.object.merge;

import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/**
 * Calculates a payload value for an {@link ObjectMask}.
 *
 * <p>This functional interface is used to compute a numeric value associated with an object mask,
 * which can be used in various algorithms, such as object merging or prioritization.
 */
@FunctionalInterface
public interface PayloadCalculator {

    /**
     * Calculates the payload value for a given object mask.
     *
     * @param object the {@link ObjectMask} for which to calculate the payload
     * @return the calculated payload value as a double
     * @throws FeatureCalculationException if an error occurs during the calculation
     */
    double calculate(ObjectMask object) throws FeatureCalculationException;
}
