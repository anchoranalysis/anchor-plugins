/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.orientation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.bound.BoundCalculator;
import org.anchoranalysis.anchor.mpp.bean.bound.ResolvedBound;
import org.anchoranalysis.anchor.mpp.bound.BidirectionalBound;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.orientation.Orientation;

class OrientationList {

    private BoundCalculator boundCalculator;
    private double boundsRatio;

    private List<Orientation> listOrientationsWithinBoundsRatio = new ArrayList<>();
    private List<Orientation> listOrientationsUnbounded = new ArrayList<>();

    public OrientationList(BoundCalculator boundCalculator, double boundsRatio) {
        super();
        this.boundCalculator = boundCalculator;
        this.boundsRatio = boundsRatio;
    }

    public void addOrientationIfUseful(
            Orientation orientation, Mark mark, ResolvedBound minMaxBound)
            throws ProposalAbnormalFailureException {

        BidirectionalBound bib;
        try {
            bib = boundCalculator.calcBound(mark.centerPoint(), orientation.createRotationMatrix());
        } catch (OperationFailedException e) {
            throw new ProposalAbnormalFailureException(e);
        }

        if (bib == null) {
            return;
        }

        if (bib.isUnboundedAtBothEnds()) {
            return;
        }

        if (bib.isUnbounded()) {

            double max = bib.getMaxOfMax();
            if (max < minMaxBound.getMax()) {
                listOrientationsUnbounded.add(orientation);
            }
        }

        if (bib.ratioBounds() > boundsRatio) {
            return;
        }

        double max = bib.getMaxOfMax();

        if (max > minMaxBound.getMax()) {
            return;
        }

        listOrientationsWithinBoundsRatio.add(orientation);

        // We deliberately don't consider the max, if it is outside our mark
        // We record it as a possible value, and we pick from the arraylist at the end
    }

    // We adopt the following priority
    //		If there are orientations within the Bounds Ratio, WE SAMPLE UNIFORMLY FROM THEM
    //		If not, and there are unbounded orientations, WE SAMPLE UNIFORMLY FROM THEM
    public Optional<Orientation> sample(RandomNumberGenerator randomNumberGenerator) {

        if (!listOrientationsWithinBoundsRatio.isEmpty()) {
            return Optional.of(
                    randomNumberGenerator.sampleFromList(listOrientationsWithinBoundsRatio));
        } else if (!listOrientationsUnbounded.isEmpty()) {
            return Optional.of(randomNumberGenerator.sampleFromList(listOrientationsUnbounded));
        } else {
            return Optional.empty();
        }
    }
}
