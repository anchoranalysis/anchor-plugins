/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.session;

import lombok.AllArgsConstructor;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.orientation.Orientation3DEulerAngles;

@AllArgsConstructor
public class MarkFixture {

    private final ImageDimensions dimensions;

    // Intersects with Ellipsoid2
    public MarkEllipsoid createEllipsoid1() {

        MarkEllipsoid ell = new MarkEllipsoid();
        ell.setMarksExplicit(
                new Point3d(17, 29, 8),
                new Orientation3DEulerAngles(2.1, 1.2, 2.0),
                new Point3d(5, 11, 3));

        assert nonZeroBBoxVolume(ell);
        return ell;
    }

    // Intersects with Ellipsoid1
    public MarkEllipsoid createEllipsoid2() {

        MarkEllipsoid ell = new MarkEllipsoid();
        ell.setMarksExplicit(
                new Point3d(20, 32, 9),
                new Orientation3DEulerAngles(2.1, 1.2, 2.0),
                new Point3d(5, 11, 6));

        assert nonZeroBBoxVolume(ell);
        return ell;
    }

    public MarkEllipsoid createEllipsoid3() {

        MarkEllipsoid ell = new MarkEllipsoid();
        ell.setMarksExplicit(
                new Point3d(104, 1, 19),
                new Orientation3DEulerAngles(0.1, 0.2, 0.5),
                new Point3d(12, 21, 7));

        assert nonZeroBBoxVolume(ell);
        return ell;
    }

    private boolean nonZeroBBoxVolume(MarkEllipsoid ell) {
        return !ell.bbox(dimensions, GlobalRegionIdentifiers.SUBMARK_INSIDE).extent().isEmpty();
    }
}
