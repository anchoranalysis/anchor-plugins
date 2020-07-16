/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.radii;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.radii.RadiiProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.orientation.Orientation;

public class RadiiProposerRepeat extends RadiiProposer {

    // START BEAN PROPERTIES
    @BeanField private RadiiProposer radiiProposer;

    @BeanField private int maxIter = 20;
    // END BEAN PROPERTIES

    @Override
    public Optional<Point3d> propose(
            Point3d pos,
            RandomNumberGenerator randomNumberGenerator,
            ImageDimensions dimensions,
            Orientation orientation)
            throws ProposalAbnormalFailureException {

        for (int i = 0; i < maxIter; i++) {
            Optional<Point3d> point =
                    radiiProposer.propose(pos, randomNumberGenerator, dimensions, orientation);
            if (point.isPresent()) {
                return point;
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return radiiProposer.isCompatibleWith(testMark);
    }

    public RadiiProposer getRadiiProposer() {
        return radiiProposer;
    }

    public void setRadiiProposer(RadiiProposer radiiProposer) {
        this.radiiProposer = radiiProposer;
    }

    public int getMaxIter() {
        return maxIter;
    }

    public void setMaxIter(int maxIter) {
        this.maxIter = maxIter;
    }
}
