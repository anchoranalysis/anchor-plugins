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

package org.anchoranalysis.plugin.mpp.bean.proposer.merge;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.core.orientation.Orientation2D;
import org.anchoranalysis.mpp.bean.proposer.MarkMergeProposer;
import org.anchoranalysis.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.conic.ConicBase;
import org.anchoranalysis.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.mpp.points.PointClamper;
import org.anchoranalysis.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.mpp.proposer.ProposerContext;
import org.anchoranalysis.spatial.point.Point3d;

public class MergeMarkEllipse extends MarkMergeProposer {

    private static final double JITTER_HALF_WIDTH = 2.0;

    // START BEAN
    @BeanField @Getter @Setter private MarkProposer markProposer = null;

    @BeanField @Getter @Setter private double mergedPosTolerance = 0.2; // formerly 0.2
    // END BEAN

    @Override
    public Optional<Mark> propose(
            VoxelizedMarkMemo mark1, VoxelizedMarkMemo mark2, ProposerContext context)
            throws ProposalAbnormalFailureException {

        VoxelizedMarkMemo targetPmm = mark1.duplicateFresh();
        ConicBase target = (ConicBase) targetPmm.getMark();

        // We calculate a position for the mark
        Point3d pos =
                generateMergedPosition(
                        mark1.getMark(),
                        mark2.getMark(),
                        mergedPosTolerance,
                        context.getRandomNumberGenerator(),
                        context.getEnergyStack());

        target.setMarksExplicit(pos);

        try {
            if (!markProposer.propose(targetPmm, context)) {
                return Optional.empty();
            }
        } catch (ProposalAbnormalFailureException e) {
            throw new ProposalAbnormalFailureException("Failed to propose mark", e);
        }

        return Optional.of(targetPmm.getMark());
    }

    private static Point3d generateMergedPosition(
            Mark mark1,
            Mark mark2,
            double posTolerance,
            RandomNumberGenerator randomNumberGenerator,
            EnergyStack energyStack) {

        Point3d pointNew = new Point3d(mark1.centerPoint());
        pointNew.subtract(mark2.centerPoint());

        // Gives us half way between the marks

        pointNew.scale(0.5 + randomNumberGenerator.sampleDoubleFromZeroCenteredRange(posTolerance));

        pointNew.add(mark2.centerPoint());

        // We add some randomness around this point
        pointNew.add(
                new Point3d(
                        randomNumberGenerator.sampleDoubleFromZeroCenteredRange(JITTER_HALF_WIDTH),
                        randomNumberGenerator.sampleDoubleFromZeroCenteredRange(JITTER_HALF_WIDTH),
                        randomNumberGenerator.sampleDoubleFromZeroCenteredRange(
                                JITTER_HALF_WIDTH)));

        return PointClamper.clamp(pointNew, energyStack.dimensions());
    }

    @SuppressWarnings("unused")
    private static Orientation2D generateOrientationBetweenPoints(Point3d pointA, Point3d pointB) {

        // We get the relative vector between them
        Point3d rel = new Point3d(pointA);
        rel.subtract(pointB);

        // Calculation an angle from this vector
        return new Orientation2D(Math.atan2(rel.y(), rel.x()));
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof ConicBase && markProposer.isCompatibleWith(testMark);
    }
}
