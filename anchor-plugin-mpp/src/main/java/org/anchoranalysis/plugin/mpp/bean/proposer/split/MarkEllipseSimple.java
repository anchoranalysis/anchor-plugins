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

package org.anchoranalysis.plugin.mpp.bean.proposer.split;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkSplitProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.anchor.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.anchor.mpp.pair.PairPxlMarkMemo;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point2d;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.orientation.Orientation2D;
import org.anchoranalysis.math.rotation.RotationMatrix;

public class MarkEllipseSimple extends MarkSplitProposer {

    /** The maximum wiggle size (added or subtracted to each end) */
    private static final int WIGGLE_MAX_SIZE = 2;

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double minRadScaleStart = 0.3;

    @BeanField @Getter @Setter private double minRadScaleEnd = 0.55;

    @BeanField @Getter @Setter private MarkProposer markProposer;
    // END BEAN PROPERTIES

    @Override
    public Optional<PairPxlMarkMemo> propose(
            VoxelizedMarkMemo mark, ProposerContext context, CfgGen cfgGen)
            throws ProposalAbnormalFailureException {

        MarkEllipse markCast = (MarkEllipse) mark.getMark();

        Orientation2D orientation = (Orientation2D) markCast.getOrientation();

        if (markCast.getRadii().x() > markCast.getRadii().y()) {
            // We do not correct the orientation
            return createMarksForOrientation(orientation, mark, context, cfgGen, false);
        } else {

            VoxelizedMarkMemo markCorrected = correctEllipseSoXAxisLongest(mark);
            // We change the orientation
            return createMarksForOrientation(orientation, markCorrected, context, cfgGen, false);
        }
    }

    private VoxelizedMarkMemo correctEllipseSoXAxisLongest(VoxelizedMarkMemo pmmExst) {

        MarkEllipse exst = (MarkEllipse) pmmExst.getMark();

        VoxelizedMarkMemo pmmDup = pmmExst.duplicateFresh();
        MarkEllipse dup = (MarkEllipse) pmmDup.getMark();

        Orientation2D exstOrientation = (Orientation2D) exst.getOrientation();

        dup.setMarks(
                new Point2d(exst.getRadii().y(), exst.getRadii().x()),
                new Orientation2D(exstOrientation.getAngleRadians() + (Math.PI / 2)));
        return pmmDup;
    }

    private Optional<PairPxlMarkMemo> createMarksForOrientation(
            Orientation2D orientation,
            VoxelizedMarkMemo markExst,
            ProposerContext context,
            CfgGen cfgGen,
            boolean wigglePos)
            throws ProposalAbnormalFailureException {

        MarkEllipse markExstCast = (MarkEllipse) markExst.getMark();

        Optional<Point3d[]> pointArr =
                createNewMarkPos(
                        orientation,
                        markExstCast,
                        context.getRandomNumberGenerator(),
                        context.dimensions(),
                        minRadScaleStart,
                        minRadScaleEnd,
                        wigglePos);

        if (!pointArr.isPresent()) {
            return Optional.empty();
        }

        Orientation2D exstOrientation = (Orientation2D) markExstCast.getOrientation();
        Orientation2D orientationRight =
                new Orientation2D(exstOrientation.getAngleRadians() + (Math.PI / 2));

        VoxelizedMarkMemo markNew1 =
                extractMemo(markExst, pointArr.get(), 0, orientationRight, context, cfgGen, "pos1");

        VoxelizedMarkMemo markNew2 =
                extractMemo(markExst, pointArr.get(), 1, orientationRight, context, cfgGen, "pos2");

        return Optional.of(new PairPxlMarkMemo(markNew1, markNew2));
    }

    private VoxelizedMarkMemo extractMemo(
            VoxelizedMarkMemo markExst,
            Point3d[] pointArr,
            int dimensionIndex,
            Orientation2D orientationRight,
            ProposerContext context,
            CfgGen cfgGen,
            String positionDescription)
            throws ProposalAbnormalFailureException {
        try {
            VoxelizedMarkMemo pmm =
                    createMarkAtPos(markExst, pointArr[dimensionIndex], orientationRight, context);
            pmm.getMark().setId(cfgGen.idAndIncrement());
            return pmm;
        } catch (ProposalAbnormalFailureException e) {
            throw new ProposalAbnormalFailureException(
                    "Failed to create mark at " + positionDescription);
        }
    }

    private VoxelizedMarkMemo createMarkAtPos(
            VoxelizedMarkMemo exst, Point3d pos, Orientation2D orientation, ProposerContext context)
            throws ProposalAbnormalFailureException {

        VoxelizedMarkMemo exstMark = exst.duplicateFresh();
        MarkEllipse mark = (MarkEllipse) exstMark.getMark();

        // THE OLD WAY WAS TO SHIFT 90 from orientation (the existing orientation)
        mark.setMarksExplicit(
                pos,
                new Orientation2D(orientation.getAngleRadians() + (Math.PI / 2)),
                mark.getRadii());
        exstMark.reset();

        markProposer.propose(exstMark, context);
        return exstMark;
    }

    public static Optional<Point3d[]> createNewMarkPos(
            Orientation2D orientation,
            MarkEllipse markExst,
            RandomNumberGenerator randomNumberGenerator,
            ImageDimensions dimensions,
            double minRadScaleStart,
            double minRadScaleEnd,
            boolean wigglePos) {

        double extent =
                markExst.getRadii().x()
                        * randomNumberGenerator.sampleDoubleFromRange(
                                minRadScaleStart, minRadScaleEnd);

        RotationMatrix rotMat = orientation.createRotationMatrix();
        double[] pointArr1 = rotMat.calcRotatedPoint(new double[] {-1 * extent, 0});
        double[] pointArr2 = rotMat.calcRotatedPoint(new double[] {extent, 0});

        Point3d point1 =
                new Point3d(
                        pointArr1[0] + markExst.getPos().x(),
                        pointArr1[1] + markExst.getPos().y(),
                        0);
        Point3d point2 =
                new Point3d(
                        pointArr2[0] + markExst.getPos().x(),
                        pointArr2[1] + markExst.getPos().y(),
                        0);

        if (wigglePos) {
            // We add some randomness around this point, say 4 pixels
            point1.add(randomPointXY(randomNumberGenerator, WIGGLE_MAX_SIZE));
            point2.add(randomPointXY(randomNumberGenerator, WIGGLE_MAX_SIZE));
        }
        return ifBothPointsInside(dimensions, point1, point2);
    }

    private static Point3d randomPointXY(
            RandomNumberGenerator randomNumberGenerator, int wiggleMaxSize) {
        return new Point3d(
                randomNumberGenerator.sampleDoubleFromZeroCenteredRange(wiggleMaxSize),
                randomNumberGenerator.sampleDoubleFromZeroCenteredRange(wiggleMaxSize),
                0);
    }

    private static Optional<Point3d[]> ifBothPointsInside(
            ImageDimensions dimensions, Point3d point1, Point3d point2) {
        if (!dimensions.contains(point1)) {
            return Optional.empty();
        }

        if (!dimensions.contains(point2)) {
            return Optional.empty();
        }

        return Optional.of(new Point3d[] {point1, point2});
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof MarkEllipse;
    }
}
