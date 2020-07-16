/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.orientation;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.bound.BoundCalculator;
import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.bound.BidirectionalBound;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractPosition;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.image.orientation.Orientation2D;

public class LongestExtent extends OrientationProposer {

    // START BEAN
    @BeanField private double incrementDegrees = 1;

    @BeanField private BoundCalculator boundCalculator;
    // END BEAN

    @Override
    public Optional<Orientation> propose(
            Mark mark, ImageDimensions dimensions, RandomNumberGenerator randomNumberGenerator)
            throws ProposalAbnormalFailureException {

        MarkAbstractPosition markC = (MarkAbstractPosition) mark;

        double incrementRadians = (incrementDegrees / 180) * Math.PI;

        double maxExtent = -1;
        double angleAtMax = 0;

        // We loop through every positive angle and pick the one with the greatest extent
        for (double angle = 0; angle < Math.PI; angle += incrementRadians) {

            BidirectionalBound bib;

            try {
                bib =
                        boundCalculator.calcBound(
                                markC.getPos(), new Orientation2D(angle).createRotationMatrix());
            } catch (OperationFailedException e) {
                throw new ProposalAbnormalFailureException(e);
            }

            double max = bib.getMaxOfMax();

            if (max > maxExtent) {
                max = maxExtent;
                angleAtMax = angle;
            }
        }

        return Optional.of(new Orientation2D(angleAtMax));
    }

    public double getIncrementDegrees() {
        return incrementDegrees;
    }

    public void setIncrementDegrees(double incrementDegrees) {
        this.incrementDegrees = incrementDegrees;
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof MarkAbstractPosition;
    }

    public BoundCalculator getBoundCalculator() {
        return boundCalculator;
    }

    public void setBoundCalculator(BoundCalculator boundCalculator) {
        this.boundCalculator = boundCalculator;
    }
}
