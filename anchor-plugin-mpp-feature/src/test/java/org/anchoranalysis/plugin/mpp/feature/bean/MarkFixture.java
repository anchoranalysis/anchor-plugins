/*-
 * #%L
 * anchor-plugin-mpp-feature
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

package org.anchoranalysis.plugin.mpp.feature.bean;

import lombok.AllArgsConstructor;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.dimensions.Dimensions;
import org.anchoranalysis.image.orientation.Orientation3DEulerAngles;
import org.anchoranalysis.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.mpp.mark.conic.Ellipsoid;

@AllArgsConstructor
public class MarkFixture {

    private final Dimensions dimensions;

    // Intersects with Ellipsoid2
    public Ellipsoid createEllipsoid1() {

        Ellipsoid ell = new Ellipsoid();
        ell.setMarksExplicit(
                new Point3d(17, 29, 8),
                new Orientation3DEulerAngles(2.1, 1.2, 2.0),
                new Point3d(5, 11, 3));

        assert nonZeroBBoxVolume(ell);
        return ell;
    }

    // Intersects with Ellipsoid1
    public Ellipsoid createEllipsoid2() {

        Ellipsoid ell = new Ellipsoid();
        ell.setMarksExplicit(
                new Point3d(20, 32, 9),
                new Orientation3DEulerAngles(2.1, 1.2, 2.0),
                new Point3d(5, 11, 6));

        assert nonZeroBBoxVolume(ell);
        return ell;
    }

    public Ellipsoid createEllipsoid3() {

        Ellipsoid ell = new Ellipsoid();
        ell.setMarksExplicit(
                new Point3d(104, 1, 19),
                new Orientation3DEulerAngles(0.1, 0.2, 0.5),
                new Point3d(12, 21, 7));

        assert nonZeroBBoxVolume(ell);
        return ell;
    }

    private boolean nonZeroBBoxVolume(Ellipsoid ell) {
        return !ell.box(dimensions, GlobalRegionIdentifiers.SUBMARK_INSIDE).extent().isEmpty();
    }
}
