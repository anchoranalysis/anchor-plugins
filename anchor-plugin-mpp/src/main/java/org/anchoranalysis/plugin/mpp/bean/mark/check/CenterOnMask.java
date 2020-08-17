/*-
 * #%L
 * anchor-plugin-mpp
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

package org.anchoranalysis.plugin.mpp.bean.mark.check;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

public class CenterOnMask extends CheckMarkBinaryChnl {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean suppressZ = false;
    // END BEAN BEAN PROPERTIES

    @Override
    public boolean check(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack)
            throws CheckException {
        return isPointOnBinaryChnl(mark.centerPoint(), nrgStack, this::derivePoint);
    }

    private Point3i derivePoint(Point3d center) {
        Point3i centerAsInt = PointConverter.intFromDoubleFloor(center);

        if (suppressZ) {
            centerAsInt.setZ(0);
        }

        return centerAsInt;
    }
}
