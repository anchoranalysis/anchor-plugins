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
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkCircle;
import org.anchoranalysis.anchor.mpp.mark.conic.RegionMapSingleton;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectMask;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CircleObjectFixture {

    private static final ImageDimensions DIMS = new ImageDimensions(800, 600, 1);

    public static ObjectMask circleAt(Point2i center, double radius) {
        MarkCircle mark = new MarkCircle();
        mark.setPos(PointConverter.doubleFromInt(center));
        mark.setRadius(radius);
        return mark.calcMask(
                        DIMS,
                        RegionMapSingleton.instance()
                                .membershipWithFlagsForIndex(
                                        GlobalRegionIdentifiers.SUBMARK_INSIDE),
                        BinaryValuesByte.getDefault())
                .getMask();
    }

    public static NRGStackWithParams nrgStack() {
        return new NRGStackWithParams(DIMS);
    }

    public static boolean sceneContains(Point2i point) {
        return DIMS.contains(PointConverter.convertTo3d(point));
    }
}
