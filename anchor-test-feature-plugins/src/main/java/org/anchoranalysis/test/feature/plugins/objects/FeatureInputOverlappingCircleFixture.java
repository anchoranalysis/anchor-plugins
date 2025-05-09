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

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.feature.input.FeatureInputPairObjects;
import org.anchoranalysis.spatial.point.Point2i;

/**
 * A fixture for creating FeatureInputPairObjects with circular object masks.
 *
 * <p>This class provides utility methods to create pairs of circular object masks with different
 * configurations of overlap and size for testing purposes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeatureInputOverlappingCircleFixture {

    private static final int DEFAULT_CIRCLE_RADIUS = 30;

    private static final int DEFAULT_POS_X = 50;
    private static final int DEFAULT_POS_Y = 50;

    /**
     * Creates two object-masks of circles in different locations WITH some overlap.
     *
     * @param sameSize if true, the object-masks are the same size; otherwise, they differ in size
     * @return a FeatureInputPairObjects populated with the two overlapping circular object-masks
     */
    public static FeatureInputPairObjects twoOverlappingCircles(boolean sameSize) {
        return twoCircles(10, 0, sameSize, 3);
    }

    /**
     * Creates two object-masks of circles in different locations WITHOUT any overlap.
     *
     * @param sameSize if true, the object-masks are the same size; otherwise, they differ in size
     * @return a FeatureInputPairObjects populated with the two non-overlapping circular
     *     object-masks
     */
    public static FeatureInputPairObjects twoNonOverlappingCircles(boolean sameSize) {
        return twoCircles(0, (DEFAULT_CIRCLE_RADIUS * 3), sameSize, -3);
    }

    private static FeatureInputPairObjects twoCircles(
            int shiftPositionX, int shiftPositionY, boolean sameSize, int extraRadius) {
        return new FeatureInputPairObjects(
                CircleObjectFixture.circleAt(position(0, 0), DEFAULT_CIRCLE_RADIUS),
                CircleObjectFixture.circleAt(
                        position(shiftPositionX, shiftPositionY),
                        radiusMaybeExtra(sameSize, extraRadius)),
                Optional.of(CircleObjectFixture.energyStack()));
    }

    private static Point2i position(int shiftPositionX, int shiftPositionY) {
        return new Point2i(DEFAULT_POS_X + shiftPositionX, DEFAULT_POS_Y + shiftPositionY);
    }

    /** If flag is true, adds extra to the default radius value */
    private static int radiusMaybeExtra(boolean flag, int extra) {
        if (flag) {
            return DEFAULT_CIRCLE_RADIUS;
        } else {
            return DEFAULT_CIRCLE_RADIUS + extra;
        }
    }
}
