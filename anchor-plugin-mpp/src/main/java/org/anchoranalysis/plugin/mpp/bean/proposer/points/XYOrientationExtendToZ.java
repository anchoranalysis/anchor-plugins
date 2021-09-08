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

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.Provider;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.unitvalue.distance.DistanceVoxels;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.core.orientation.Orientation;
import org.anchoranalysis.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.mpp.bean.proposer.PointsProposer;
import org.anchoranalysis.mpp.bean.proposer.ScalarProposer;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.plugin.mpp.bean.outline.TraverseOutlineException;
import org.anchoranalysis.plugin.mpp.bean.proposer.points.fromorientation.PointsFromOrientationProposer;
import org.anchoranalysis.spatial.axis.AxisType;
import org.anchoranalysis.spatial.point.Point3d;
import org.anchoranalysis.spatial.point.Point3i;

public class XYOrientationExtendToZ extends PointsProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private OrientationProposer orientationXYProposer; // Should find an orientation in the XY plane

    @BeanField @Getter @Setter
    private PointsFromOrientationProposer pointsFromOrientationXYProposer;

    @BeanField @Getter @Setter private Provider<Mask> mask;

    @BeanField @OptionalBean @Getter @Setter private Provider<Mask> maskFilled;

    @BeanField @Getter @Setter private ScalarProposer maxDistanceZ;

    @BeanField @Getter @Setter private int minNumSlices = 3;

    @BeanField @Getter @Setter private boolean forwardDirectionOnly = false;

    // If we reach this amount of slices without adding a point, we consider our job over
    @BeanField @Getter @Setter
    private UnitValueDistance distanceZEndIfEmpty = new DistanceVoxels(1000000);
    // END BEAN PROPERTIES

    @Override
    public Optional<List<Point3i>> propose(
            Point3d point,
            Mark mark,
            Dimensions dimensions,
            RandomNumberGenerator randomNumberGenerator,
            ErrorNode errorNode)
            throws ProposalAbnormalFailureException {

        Optional<Orientation> orientation =
                orientationXYProposer.propose(mark, dimensions, randomNumberGenerator);

        return orientation.flatMap(
                or ->
                        proposeFromOrientation(
                                or, point, dimensions, randomNumberGenerator, errorNode));
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return orientationXYProposer.isCompatibleWith(testMark);
    }

    private Optional<List<Point3i>> proposeFromOrientation(
            Orientation orientation,
            Point3d point,
            Dimensions dimensions,
            RandomNumberGenerator randomNumberGenerator,
            ErrorNode errorNode) {
        try {
            List<List<Point3i>> pointsXY =
                    getPointsFromOrientationXYProposer()
                            .calculatePoints(
                                    point,
                                    orientation,
                                    dimensions.z() > 1,
                                    randomNumberGenerator,
                                    forwardDirectionOnly);

            List<Point3i> lastPointsAll =
                    new GeneratePointsHelper(
                                    point,
                                    channelFilled(),
                                    maxZDistance(randomNumberGenerator, dimensions.resolution()),
                                    skipZDistance(dimensions.resolution()),
                                    mask.get(),
                                    dimensions)
                            .generatePoints(pointsXY);
            return Optional.of(lastPointsAll);

        } catch (ProvisionFailedException
                | OperationFailedException
                | TraverseOutlineException e1) {
            errorNode.add(e1);
            return Optional.empty();
        }
    }

    private int maxZDistance(
            RandomNumberGenerator randomNumberGenerator, Optional<Resolution> resolution)
            throws OperationFailedException {
        int maxZDistance =
                (int) Math.round(maxDistanceZ.propose(randomNumberGenerator, resolution));
        maxZDistance = Math.max(maxZDistance, minNumSlices);
        return maxZDistance;
    }

    private int skipZDistance(Optional<Resolution> resolution) throws OperationFailedException {
        return (int)
                Math.round(
                        distanceZEndIfEmpty.resolveForAxis(
                                resolution.map(Resolution::unitConvert), AxisType.Z));
    }

    private Optional<Mask> channelFilled() throws ProvisionFailedException {
        return maskFilled != null ? Optional.of(maskFilled.get()) : Optional.empty();
    }
}
