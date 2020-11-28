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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.spatial.point.Point3i;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ArrayExtracter {

    /**
     * Extracts a point encoded within a string in a CSV file.
     *
     * <p>The point is encoded either as:
     *
     * <ul>
     *   <li>{@code 10_20_30} for x, y and z (integer) coordinates of a single points.
     *   <li>{@code 10_20_30_and_40_50_60} or for coordinates of two points.
     * </ul>
     *
     * @param array the array to extract from
     * @param indexColumnUniquePixel the index of a column describing a unique-pixel within an
     *     object.
     * @param first if there are two points encoded in the <i>unique-pixel</i> column, rather than
     *     one, then whether to select the first or the second.
     * @return a point constructed from converting the elements for X, Y, Z into integers
     */
    public static Point3i getAsPoint(String[] array, int indexColumnUniquePixel, boolean first) {

        String content = array[indexColumnUniquePixel];

        String[] components = content.split("_");

        if (first) {
            return pointFromComponents(components, 0);
        } else {
            return pointFromComponents(components, 4);
        }
    }

    private static Point3i pointFromComponents(String[] components, int startIndex) {
        return new Point3i(
                Integer.parseInt(components[startIndex]),
                Integer.parseInt(components[startIndex + 1]),
                Integer.parseInt(components[startIndex + 2]));
    }
}
