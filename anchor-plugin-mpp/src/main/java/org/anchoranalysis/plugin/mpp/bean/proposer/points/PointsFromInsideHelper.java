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

import com.google.common.base.Preconditions;
import org.anchoranalysis.image.convert.UnsignedByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import one.util.streamex.IntStreamEx;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.box.BoundingBox;
import org.anchoranalysis.image.voxel.Voxels;

class PointsFromInsideHelper {

    private final PointListForConvex pointsConvexRoot;

    private final BoundingBox boundingBox;
    private final BinaryVoxels<UnsignedByteBuffer> voxelsFilled;
    private final ReadableTuple3i cornerMin;
    private final ReadableTuple3i cornerMax;

    public PointsFromInsideHelper(
            PointListForConvex pointsConvexRoot, Mask maskFilled, BoundingBox box) {
        this.pointsConvexRoot = pointsConvexRoot;
        this.boundingBox = box;
        this.voxelsFilled = maskFilled.binaryVoxels();
        this.cornerMin = box.cornerMin();
        this.cornerMax = box.calculateCornerMax();
    }

    public List<Point3i> convexOnly(
            Mask mask, Point3d pointRoot, int skipAfterSuccessiveEmptySlices) {
        Preconditions.checkArgument(mask.dimensions().contains(boundingBox));

        int startZ = (int) Math.floor(pointRoot.z());

        List<Point3i> listOut = new ArrayList<>();

        // Stays as -1 until we reach a non-empty slice
        int successiveEmptySlices = -1;

        successiveEmptySlices =
                iterateOverSlices(
                        IntStream.rangeClosed(startZ, cornerMax.z()),
                        mask,
                        successiveEmptySlices,
                        skipAfterSuccessiveEmptySlices,
                        listOut::add);

        // Exit early if we start on the first slice
        if (startZ == 0) {
            return listOut;
        }
        iterateOverSlices(
                IntStreamEx.rangeClosed(startZ - 1, cornerMin.z(), -1),
                mask,
                successiveEmptySlices,
                skipAfterSuccessiveEmptySlices,
                listOut::add);

        return listOut;
    }

    private int iterateOverSlices(
            IntStream zRange,
            Mask mask,
            int successiveEmptySlices,
            int skipAfterSuccessiveEmptySlices,
            Consumer<Point3i> processPoint) {
        BinaryValuesByte bvb = mask.binaryValues().createByte();

        Voxels<UnsignedByteBuffer> voxels = mask.channel().voxels().asByte();
        Extent extent = voxels.extent();

        Iterator<Integer> itr = zRange.iterator();
        while (itr.hasNext()) {
            int z = itr.next();

            UnsignedByteBuffer buffer = voxels.sliceBuffer(z);

            if (!addPointsToSlice(extent, buffer, bvb, z, processPoint)) {
                successiveEmptySlices = 0;
            } else if (successiveEmptySlices != -1) {
                // We don't increase the counter until we've been inside a non-empty slice
                successiveEmptySlices++;
                if (successiveEmptySlices >= skipAfterSuccessiveEmptySlices) {
                    break;
                }
            }
        }

        return successiveEmptySlices;
    }

    private boolean addPointsToSlice(
            Extent extent,
            UnsignedByteBuffer buffer,
            BinaryValuesByte bvb,
            int z,
            Consumer<Point3i> processPoint) {
        boolean addedToSlice = false;
        for (int y = cornerMin.y(); y <= cornerMax.y(); y++) {
            for (int x = cornerMin.x(); x <= cornerMax.x(); x++) {
                int offset = extent.offset(x, y);
                if (buffer.getRaw(offset) == bvb.getOnByte()) {

                    Point3i point = new Point3i(x, y, z);
                    if (pointsConvexRoot.convexWithAtLeastOnePoint(point, voxelsFilled)) {
                        addedToSlice = true;
                        processPoint.accept(point);
                    }
                }
            }
        }
        return addedToSlice;
    }
}
