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

package org.anchoranalysis.plugin.mpp.bean.check;

import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.mpp.feature.error.CheckException;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.point.Point3d;
import org.anchoranalysis.spatial.point.Point3i;
import org.anchoranalysis.spatial.point.ReadableTuple3i;

/**
 * The center ps of at least one slice should be on the stack Tries overall center point first, and
 * then projects it onto each slice
 *
 * @author Owen Feehan
 */
public class AnySliceCenterOnMask extends CheckMarkWithMask {

    @Override
    public boolean check(Mark mark, RegionMap regionMap, EnergyStack energyStack)
            throws CheckException {
        Point3d cp = mark.centerPoint();
        if (isPointOnMask(cp, energyStack, AnySliceCenterOnMask::derivePoint)) {
            return true;
        }

        BoundingBox box = mark.boxAllRegions(energyStack.dimensions());
        ReadableTuple3i cornerMax = box.calculateCornerMax();
        for (int z = box.cornerMin().z(); z <= cornerMax.z(); z++) {
            Point3d cpSlice = new Point3d(cp.x(), cp.y(), z);
            if (isPointOnMask(cpSlice, energyStack, AnySliceCenterOnMask::derivePoint)) {
                return true;
            }
        }

        return false;
    }

    private static Point3i derivePoint(Point3d cp) {
        return new Point3i((int) cp.x(), (int) cp.y(), (int) cp.z());
    }
}
