/*-
 * #%L
 * anchor-test-feature-plugins
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

package org.anchoranalysis.test.feature.plugins.objects;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.mpp.bean.regionmap.RegionMapSingleton;
import org.anchoranalysis.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.mpp.mark.conic.Circle;
import org.anchoranalysis.spatial.point.Point2i;
import org.anchoranalysis.spatial.point.PointConverter;

/**
 * A fixture for creating circular objects and related utilities for testing purposes.
 *
 * <p>This class provides methods to create circular object masks, energy stacks, and to check if
 * points are within the scene dimensions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CircleObjectFixture {

    private static final Dimensions DIMS = new Dimensions(800, 600, 1);

    /**
     * Creates a circular object mask at a specified center point with a given radius.
     *
     * @param center the center point of the circle in 2D coordinates
     * @param radius the radius of the circle
     * @return an ObjectMask representing the circular object
     */
    public static ObjectMask circleAt(Point2i center, double radius) {
        Circle mark = new Circle();
        mark.setPosition(PointConverter.doubleFromInt(center));
        mark.setRadius(radius);
        return mark.deriveObject(
                DIMS,
                RegionMapSingleton.instance()
                        .membershipWithFlagsForIndex(GlobalRegionIdentifiers.SUBMARK_INSIDE),
                BinaryValuesByte.getDefault());
    }

    /**
     * Creates an EnergyStack with the fixture's dimensions.
     *
     * @return a new EnergyStack instance
     */
    public static EnergyStack energyStack() {
        return new EnergyStack(DIMS);
    }

    /**
     * Checks if a given 2D point is within the scene dimensions.
     *
     * @param point the 2D point to check
     * @return true if the point is within the scene dimensions, false otherwise
     */
    public static boolean sceneContains(Point2i point) {
        return DIMS.contains(PointConverter.convertTo3i(point));
    }
}
