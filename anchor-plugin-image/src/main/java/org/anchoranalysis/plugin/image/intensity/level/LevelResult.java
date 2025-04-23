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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.math.histogram.Histogram;
import org.anchoranalysis.spatial.point.Point3i;

/**
 * Represents the result of a level operation, containing the level value, object mask, and
 * histogram.
 */
@AllArgsConstructor
public class LevelResult {

    /** The level value. */
    @Getter private final int level;

    /** The {@link ObjectMask} associated with this level. */
    @Getter private final ObjectMask object;

    /** The {@link Histogram} of intensity values for this level. */
    @Getter private final Histogram histogram;

    /**
     * Calculates the squared distance from the midpoint of this level's object to a given point.
     *
     * @param sourcePoint the {@link Point3i} to calculate the distance to
     * @return the squared distance between the midpoint of this level's object and the given point
     */
    public double distanceSquaredTo(Point3i sourcePoint) {
        return object.boundingBox().midpoint().distanceSquared(sourcePoint);
    }
}
