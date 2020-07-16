/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.orientation;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.image.orientation.Orientation2D;

public class AngleRotation extends OrientationProposer {

    // START BEAN PROPERTIES
    @BeanField private double angleDegrees = 0;
    // END BEAN PROPERTIES

    @Override
    public Optional<Orientation> propose(
            Mark mark, ImageDimensions dimensions, RandomNumberGenerator randomNumberGenerator) {
        MarkEllipse markC = (MarkEllipse) mark;

        Orientation2D exstOrientation = (Orientation2D) markC.getOrientation();

        double angleRadians = (angleDegrees / 180) * Math.PI;
        return Optional.of(new Orientation2D(exstOrientation.getAngleRadians() + angleRadians));
    }

    public double getAngleDegrees() {
        return angleDegrees;
    }

    public void setAngleDegrees(double angleDegrees) {
        this.angleDegrees = angleDegrees;
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof MarkEllipse;
    }
}
