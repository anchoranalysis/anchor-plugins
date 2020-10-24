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

package org.anchoranalysis.plugin.mpp.bean.proposer.points.onoutline;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.provider.Provider;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.core.orientation.Orientation;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.spatial.point.Point3d;
import org.anchoranalysis.spatial.point.Point3i;
import org.anchoranalysis.spatial.point.PointConverter;
import org.anchoranalysis.spatial.rotation.RotationMatrix;

/** Walks in a particular direction until the outline is found. */
public class FindPointOnOutlineWalk extends FindPointOnOutline {

    // START BEANS
    @BeanField @Getter @Setter private Provider<Mask> mask;

    @BeanField @OptionalBean @Getter @Setter private UnitValueDistance maxDistance;
    // END BEANS

    private Mask maskCreated;
    private Channel channel;

    @Override
    public Optional<Point3i> pointOnOutline(Point3d centerPoint, Orientation orientation)
            throws OperationFailedException { // NOSONAR

        createMaskIfNecessary();

        RotationMatrix rotationMatrix = orientation.createRotationMatrix();

        boolean is3D = rotationMatrix.getNumberDimensions() >= 3;

        return pointOnOutline(centerPoint, marginalStepFrom(rotationMatrix, is3D), is3D);
    }

    private void createMaskIfNecessary() throws OperationFailedException {
        // The first time, we establish the binaryImage
        if (maskCreated == null) {
            try {
                maskCreated = mask.create();
                channel = maskCreated.channel();
            } catch (CreateException e) {
                throw new OperationFailedException(e);
            }
        }
    }

    private Optional<Point3i> pointOnOutline( // NOSONAR
            Point3d centerPoint, Point3d step, boolean useZ) throws OperationFailedException {

        BinaryValuesByte bvb = maskCreated.binaryValues().createByte();

        Point3d pointDouble = new Point3d(centerPoint);
        while (true) {
            pointDouble.increment(step);

            Point3i point = PointConverter.intFromDoubleFloor(pointDouble);

            // We do check
            if (failsDistanceCondition(centerPoint, pointDouble)) {
                return Optional.empty();
            }

            Dimensions dimensions = maskCreated.dimensions();
            if (!dimensions.contains(point)) {
                return Optional.empty();
            }

            if (pointIsOutlineVal(point, dimensions, bvb)) {
                return Optional.of(point);
            }

            point.incrementX();

            if (pointIsOutlineVal(point, dimensions, bvb)) {
                return Optional.of(point);
            }

            point.decrementX(2);

            if (pointIsOutlineVal(point, dimensions, bvb)) {
                return Optional.of(point);
            }

            point.incrementX();
            point.decrementY();

            if (pointIsOutlineVal(point, dimensions, bvb)) {
                return Optional.of(point);
            }

            point.incrementY(2);

            if (pointIsOutlineVal(point, dimensions, bvb)) {
                return Optional.of(point);
            }

            point.decrementY();

            if (useZ) {
                point.decrementZ();
                if (pointIsOutlineVal(point, dimensions, bvb)) {
                    return Optional.of(point);
                }

                point.incrementZ(2);
                if (pointIsOutlineVal(point, dimensions, bvb)) {
                    return Optional.of(point);
                }
            }
        }
    }

    private boolean pointIsOutlineVal(Point3i point, Dimensions dimensions, BinaryValuesByte bvb) {

        if (!dimensions.contains(point)) {
            return false;
        }

        UnsignedByteBuffer buffer = channel.voxels().asByte().sliceBuffer(point.z());
        return buffer.getRaw(dimensions.offsetSlice(point)) == bvb.getOnByte();
    }

    private static Point3d marginalStepFrom(RotationMatrix matrix, boolean is3d) {
        return new Point3d(
                matrix.getMatrix().get(0, 0),
                matrix.getMatrix().get(1, 0),
                is3d ? matrix.getMatrix().get(2, 0) : 0);
    }

    private boolean failsDistanceCondition(Point3d centerPoint, Point3d pointDouble)
            throws OperationFailedException {
        // We do check
        if (maxDistance != null) {
            double distance =
                    distanceZBetweenPoints(centerPoint, pointDouble, maskCreated.resolution());
            double maxDistanceResolved =
                    maxDistance.resolve(
                            maskCreated.dimensions().resolution().map(Resolution::unitConvert),
                            centerPoint,
                            pointDouble);
            return distance > maxDistanceResolved;
        } else {
            return false;
        }
    }

    private static double distanceZBetweenPoints(
            Point3d centerPoint, Point3d pointDouble, Optional<Resolution> resolution) {
        if (resolution.isPresent()) {
            return resolution.get().distanceZRelative(centerPoint, pointDouble);
        } else {
            return Math.abs(centerPoint.z() - pointDouble.z());
        }
    }
}
