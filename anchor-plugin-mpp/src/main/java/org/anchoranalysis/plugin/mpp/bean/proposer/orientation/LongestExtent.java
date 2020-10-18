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

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.orientation.Orientation;
import org.anchoranalysis.image.core.orientation.Orientation2D;
import org.anchoranalysis.mpp.bean.bound.BoundCalculator;
import org.anchoranalysis.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.mpp.bound.BidirectionalBound;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkWithPosition;
import org.anchoranalysis.mpp.proposer.ProposalAbnormalFailureException;

public class LongestExtent extends OrientationProposer {

    // START BEAN
    @BeanField @Getter @Setter private double incrementDegrees = 1;

    @BeanField @Getter @Setter private BoundCalculator boundCalculator;
    // END BEAN

    @Override
    public Optional<Orientation> propose(
            Mark mark, Dimensions dimensions, RandomNumberGenerator randomNumberGenerator)
            throws ProposalAbnormalFailureException {

        MarkWithPosition markC = (MarkWithPosition) mark;

        double incrementRadians = (incrementDegrees / 180) * Math.PI;

        double maxExtent = -1;
        double angleAtMax = 0;

        // We loop through every positive angle and pick the one with the greatest extent
        for (double angle = 0; angle < Math.PI; angle += incrementRadians) {

            BidirectionalBound bib;

            try {
                bib =
                        boundCalculator.calculateBound(
                                markC.getPos(), new Orientation2D(angle).createRotationMatrix());
            } catch (OperationFailedException e) {
                throw new ProposalAbnormalFailureException(e);
            }

            double max = bib.getMaxOfMax();

            if (max > maxExtent) {
                maxExtent = max;
                angleAtMax = angle;
            }
        }

        return Optional.of(new Orientation2D(angleAtMax));
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof MarkWithPosition;
    }
}
