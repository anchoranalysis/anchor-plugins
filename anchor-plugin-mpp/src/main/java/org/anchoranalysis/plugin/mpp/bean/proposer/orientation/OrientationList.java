/*-
 * #%L
 * anchor-plugin-mpp
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
