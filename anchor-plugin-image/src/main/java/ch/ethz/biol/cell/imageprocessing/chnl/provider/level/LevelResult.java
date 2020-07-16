/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider.level;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.object.ObjectMask;

@AllArgsConstructor
public class LevelResult {

    @Getter private final int level;

    @Getter private final ObjectMask object;

    @Getter private final Histogram histogram;

    public double distanceSquaredTo(Point3d srcPoint) {
        return srcPoint.distanceSquared(object.getBoundingBox().midpoint());
    }
}
