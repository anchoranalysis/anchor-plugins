/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.check;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.extent.BoundingBox;

/**
 * The center ps of at least one slice should be on the stack Tries overall center point first, and
 * then projects it onto each slice
 *
 * @author Owen Feehan
 */
public class AnySliceCenterOnMask extends CheckMarkBinaryChnl {

    @Override
    public boolean check(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack)
            throws CheckException {
        Point3d cp = mark.centerPoint();
        if (isPointOnBinaryChnl(cp, nrgStack, AnySliceCenterOnMask::derivePoint)) {
            return true;
        }

        BoundingBox bbox = mark.bboxAllRegions(nrgStack.getDimensions());
        ReadableTuple3i cornerMax = bbox.calcCornerMax();
        for (int z = bbox.cornerMin().getZ(); z <= cornerMax.getZ(); z++) {
            Point3d cpSlice = new Point3d(cp.getX(), cp.getY(), z);
            if (isPointOnBinaryChnl(cpSlice, nrgStack, AnySliceCenterOnMask::derivePoint)) {
                return true;
            }
        }

        return false;
    }

    private static Point3i derivePoint(Point3d cp) {
        return new Point3i((int) cp.getX(), (int) cp.getY(), (int) cp.getZ());
    }
}
