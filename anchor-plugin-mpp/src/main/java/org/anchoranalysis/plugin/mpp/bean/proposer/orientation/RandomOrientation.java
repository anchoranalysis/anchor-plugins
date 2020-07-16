/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.orientation;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.bounds.RotationBounds;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.orientation.Orientation;

public class RandomOrientation extends OrientationProposer {

    // START BEAN PROPERTIES
    @BeanField private RotationBounds bounds;
    // END BEAN PROPERTIES

    public RandomOrientation() {
        // Standard bean constructor
    }

    public RandomOrientation(RotationBounds bounds) {
        this.bounds = bounds;
    }

    @Override
    public Optional<Orientation> propose(
            Mark mark, ImageDimensions dimensions, RandomNumberGenerator randomNumberGenerator)
            throws ProposalAbnormalFailureException {
        return Optional.of(bounds.randomOrientation(randomNumberGenerator, dimensions.getRes()));
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }

    public RotationBounds getBounds() {
        return bounds;
    }

    public void setBounds(RotationBounds bounds) {
        this.bounds = bounds;
    }
}
