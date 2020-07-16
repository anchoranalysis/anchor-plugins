/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.merge;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkMergeProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkConic;
import org.anchoranalysis.anchor.mpp.points.PointClipper;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.orientation.Orientation2D;

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
        MarkConic target = (MarkConic) targetPmm.getMark();

        // We calculate a position for the mark
        Point3d pos =
                generateMergedPosition(
                        mark1.getMark(),
                        mark2.getMark(),
                        mergedPosTolerance,
                        context.getRandomNumberGenerator(),
                        context.getNrgStack());

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
            NRGStackWithParams nrgStack) {

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

        return PointClipper.clip(pointNew, nrgStack.getDimensions());
    }

    @SuppressWarnings("unused")
    private static Orientation2D generateOrientationBetweenPoints(Point3d pointA, Point3d pointB) {

        // We get the relative vector between them
        Point3d rel = new Point3d(pointA);
        rel.subtract(pointB);

        // Calculation an angle from this vector
        return new Orientation2D(Math.atan2(rel.getY(), rel.getX()));
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof MarkConic && markProposer.isCompatibleWith(testMark);
    }
}
