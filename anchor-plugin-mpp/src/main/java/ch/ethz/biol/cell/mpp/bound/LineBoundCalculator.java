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

package ch.ethz.biol.cell.mpp.bound;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.bound.BoundCalculator;
import org.anchoranalysis.anchor.mpp.bean.bound.ResolvedBound;
import org.anchoranalysis.anchor.mpp.bound.BidirectionalBound;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.math.rotation.RotationMatrix;

public class LineBoundCalculator extends BoundCalculator {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MaskProvider outlineProvider;

    @BeanField @Getter @Setter private double extra = 0;
    // END BEAN PROPERTIES

    @Override
    public BidirectionalBound calcBound(Point3d point, RotationMatrix rotMatrix)
            throws OperationFailedException {

        try {
            Channel outlineChnl = outlineProvider.create().channel();
            assert (outlineChnl != null);

            ResolvedBound minMax =
                    getInitializationParameters()
                            .getMarkBounds()
                            .calcMinMax(
                                    outlineChnl.dimensions().resolution(),
                                    rotMatrix.getNumDim() >= 3);

            int maxPossiblePoint = (int) Math.ceil(minMax.getMax());

            Point3d marg =
                    new Point3d(
                            rotMatrix.getMatrix().get(0, 0),
                            rotMatrix.getMatrix().get(1, 0),
                            rotMatrix.getNumDim() >= 3 ? rotMatrix.getMatrix().get(2, 0) : 0);
            Point3d margInverse = Point3d.immutableScale(marg, -1);

            // This is 2D Type of code
            double maxReachedFwd = maxReachablePoint(outlineChnl, point, marg, maxPossiblePoint);
            double maxReachedRvrs =
                    maxReachablePoint(outlineChnl, point, margInverse, maxPossiblePoint);

            double min = minMax.getMin();

            return new BidirectionalBound(
                    createBoundForDirection(min, maxReachedFwd),
                    createBoundForDirection(min, maxReachedRvrs));

        } catch (CreateException e) {
            throw new OperationFailedException(e);
        } catch (NamedProviderGetException e) {
            throw new OperationFailedException(e.summarize());
        }
    }

    private static Optional<ResolvedBound> createBoundForDirection(
            double min, double maxDirection) {

        if (maxDirection == -1 || min == -1) {
            return Optional.empty();
        }

        if (min <= maxDirection) {
            return Optional.of(new ResolvedBound(min, maxDirection));
        } else {
            return Optional.empty();
        }
    }

    private double maxReachablePoint(
            Channel channel, Point3d point, Point3d marg, int maxPossiblePoint) {

        Voxels<ByteBuffer> voxels = channel.voxels().asByte();

        // This only exists in 2d for now so we can use a slice byteArray
        ByteBuffer arr = null;

        int zPrev = 0;
        arr = voxels.slices().slice(zPrev).buffer();

        Point3d runningDbl = new Point3d();

        for (int i = 1; i < maxPossiblePoint; i++) {
            runningDbl.add(marg);

            Point3i runningInt =
                    PointConverter.intFromDoubleFloor(Point3d.immutableAdd(point, runningDbl));

            ImageDimensions dimensions = channel.dimensions();
            if (dimensions.contains(runningInt)) {
                return -1;
            }

            if (runningInt.z() != zPrev) {
                zPrev = runningInt.z();
                arr = voxels.slices().slice(zPrev).buffer();
            }

            int index = dimensions.offsetSlice(runningInt);
            int v = ByteConverter.unsignedByteToInt(arr.get(index));

            if (v > 0) {
                // We calculate how far we have travelled in total
                return extra
                        + normZMag(
                                runningDbl,
                                channel.dimensions().resolution().getZRelativeResolution());
            }
        }
        return -1;
    }

    private double normZMag(Point3d point, double zMult) {
        double dx = point.x();
        double dy = point.y();
        double dz = point.z() * zMult;
        return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
    }
}
