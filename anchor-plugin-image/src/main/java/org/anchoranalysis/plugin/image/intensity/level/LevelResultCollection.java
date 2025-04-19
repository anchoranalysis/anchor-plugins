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

package org.anchoranalysis.plugin.image.intensity.level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.anchoranalysis.spatial.point.Point3i;

/**
 * A collection of {@link LevelResult} objects that can be iterated over and searched.
 */
public class LevelResultCollection implements Iterable<LevelResult> {

    /** The list of {@link LevelResult} objects. */
    private List<LevelResult> list = new ArrayList<>();

    /**
     * Adds a {@link LevelResult} to the collection.
     *
     * @param arg0 the {@link LevelResult} to add
     * @return true if the collection changed as a result of the call
     */
    public boolean add(LevelResult arg0) {
        return list.add(arg0);
    }

    /**
     * Finds the {@link LevelResult} closest to a given point.
     *
     * @param point the {@link Point3i} to find the closest result to
     * @return the {@link LevelResult} closest to the given point, or null if the collection is empty
     */
    public LevelResult findClosestResult(Point3i point) {

        double minDistance = Double.MAX_VALUE;
        LevelResult min = null;

        for (LevelResult levelResult : list) {

            double distance = levelResult.distanceSquaredTo(point);
            if (distance < minDistance) {
                minDistance = distance;
                min = levelResult;
            }
        }
        return min;
    }

    @Override
    public Iterator<LevelResult> iterator() {
        return list.iterator();
    }
}