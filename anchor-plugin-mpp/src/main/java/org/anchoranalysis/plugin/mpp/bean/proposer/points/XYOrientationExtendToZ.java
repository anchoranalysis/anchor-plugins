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

package org.anchoranalysis.plugin.mpp.bean.proposer.points;

import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.TraverseOutlineException;
import java.awt.Color;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.PointsProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.ScalarProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.points.MarkPointListFactory;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposeVisualizationList;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.axis.AxisType;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistanceVoxels;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.plugin.mpp.bean.proposer.points.fromorientation.PointsFromOrientationProposer;

public class XYOrientationExtendToZ extends PointsProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private OrientationProposer orientationXYProposer; // Should find an orientation in the XY plane

    @BeanField @Getter @Setter
    private PointsFromOrientationProposer pointsFromOrientationXYProposer;

    @BeanField @Getter @Setter private MaskProvider binaryChnl;

    @BeanField @OptionalBean @Getter @Setter private MaskProvider binaryChnlFilled;

    @BeanField @Getter @Setter private ScalarProposer maxDistanceZ;

    @BeanField @Getter @Setter private int minNumSlices = 3;

    @BeanField @Getter @Setter private boolean forwardDirectionOnly = false;

    // If we reach this amount of slices without adding a point, we consider our job over
    @BeanField @Getter @Setter
    private UnitValueDistance distanceZEndIfEmpty = new UnitValueDistanceVoxels(1000000);
    // END BEAN PROPERTIES

    private List<Point3i> lastPointsAll;

    @Override
    public Optional<List<Point3i>> propose(
            Point3d point,
            Mark mark,
            ImageDimensions dimensions,
            RandomNumberGenerator randomNumberGenerator,
            ErrorNode errorNode)
            throws ProposalAbnormalFailureException {
        pointsFromOrientationXYProposer.clearVisualizationState();

        Optional<Orientation> orientation =
                orientationXYProposer.propose(mark, dimensions, randomNumberGenerator);

        return orientation.flatMap(
                or ->
                        proposeFromOrientation(
                                or, point, dimensions, randomNumberGenerator, errorNode));
    }

    @Override
    public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {

        CreateProposeVisualizationList list = new CreateProposeVisualizationList();

        list.add(pointsFromOrientationXYProposer.proposalVisualization(detailed));

        list.add(
                cfg -> {
                    if (lastPointsAll != null && !lastPointsAll.isEmpty()) {
                        cfg.addChangeID(
                                MarkPointListFactory.createMarkFromPoints3i(lastPointsAll),
                                new RGBColor(Color.ORANGE));
                    }
                });

        return Optional.of(list);
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return orientationXYProposer.isCompatibleWith(testMark);
    }

    private Optional<List<Point3i>> proposeFromOrientation(
            Orientation orientation,
            Point3d point,
            ImageDimensions dimensions,
            RandomNumberGenerator randomNumberGenerator,
            ErrorNode errorNode) {
        try {
            List<List<Point3i>> pointsXY =
                    getPointsFromOrientationXYProposer()
                            .calcPoints(
                                    point,
                                    orientation,
                                    dimensions.z() > 1,
                                    randomNumberGenerator,
                                    forwardDirectionOnly);

            lastPointsAll =
                    new GeneratePointsHelper(
                                    point,
                                    chnlFilled(),
                                    maxZDistance(randomNumberGenerator, dimensions.resolution()),
                                    skipZDistance(dimensions.resolution()),
                                    binaryChnl.create(),
                                    dimensions)
                            .generatePoints(pointsXY);
            return Optional.of(lastPointsAll);

        } catch (CreateException | OperationFailedException | TraverseOutlineException e1) {
            errorNode.add(e1);
            return Optional.empty();
        }
    }

    private int maxZDistance(
            RandomNumberGenerator randomNumberGenerator, ImageResolution resolution)
            throws OperationFailedException {
        int maxZDistance =
                (int) Math.round(maxDistanceZ.propose(randomNumberGenerator, resolution));
        maxZDistance = Math.max(maxZDistance, minNumSlices);
        return maxZDistance;
    }

    private int skipZDistance(ImageResolution res) throws OperationFailedException {
        return (int) Math.round(distanceZEndIfEmpty.resolveForAxis(Optional.of(res), AxisType.Z));
    }

    private Optional<Mask> chnlFilled() throws CreateException {
        return binaryChnlFilled != null ? Optional.of(binaryChnlFilled.create()) : Optional.empty();
    }
}
