/* (C)2020 */
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
        Point3i centerAsInt = PointConverter.intFromDouble(center);

        if (suppressZ) {
            centerAsInt.setZ(0);
        }

        return centerAsInt;
    }
}
