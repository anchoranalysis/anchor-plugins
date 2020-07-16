/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.radii;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.bound.Bound;
import org.anchoranalysis.anchor.mpp.bean.proposer.radii.RadiiProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkConic;
import org.anchoranalysis.anchor.mpp.mark.conic.RadiiRandomizer;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.orientation.Orientation;

public class UniformRandomRadiiProposer extends RadiiProposer {

    // START BEAN PROPERTIES
    @BeanField private Bound radiusBound;

    @BeanField private boolean do3D = false;
    // END BEAN PROPERTIES

    public UniformRandomRadiiProposer() {
        // Standard Bean Constructor
    }

    public UniformRandomRadiiProposer(Bound radiusBound, boolean do3D) {
        this.radiusBound = radiusBound;
        this.do3D = do3D;
    }

    @Override
    public Optional<Point3d> propose(
            Point3d pos,
            RandomNumberGenerator randomNumberGenerator,
            ImageDimensions dimensions,
            Orientation orientation)
            throws ProposalAbnormalFailureException {
        return Optional.of(
                RadiiRandomizer.randomizeRadii(
                        radiusBound, randomNumberGenerator, dimensions.getRes(), do3D));
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof MarkConic;
    }

    public Bound getRadiusBound() {
        return radiusBound;
    }

    public void setRadiusBound(Bound radiusBound) {
        this.radiusBound = radiusBound;
    }

    public boolean isDo3D() {
        return do3D;
    }

    public void setDo3D(boolean do3d) {
        do3D = do3d;
    }
}
