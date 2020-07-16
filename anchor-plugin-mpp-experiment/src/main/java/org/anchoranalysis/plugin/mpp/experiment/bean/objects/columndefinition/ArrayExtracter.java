/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.mpp.experiment.bean.objects.columndefinition;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.geometry.Point3i;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ArrayExtracter {

    /**
     * Extracts a point from three indices in string-array
     *
     * @param array the array to extract from
     * @param indices a three element array with the indices for X, Y, Z component of the point
     *     respectively
     * @return a point constructed from converting the elements for X, Y, Z into integers
     */
    public static Point3i getAsPoint(String[] array, int[] indices) {
        Preconditions.checkArgument(indices.length == 3);
        return new Point3i(
                getAsInt(array, indices[0]),
                getAsInt(array, indices[1]),
                getAsInt(array, indices[2]));
    }

    /**
     * Gets a particular index in a string-array, and converts it to an integer
     *
     * @param array the array to extract from
     * @param index the particular index to extract
     * @return the element at that index converted into an {@code int}
     */
    public static int getAsInt(String[] array, int index) {
        return Integer.parseInt(array[index]);
    }
}
