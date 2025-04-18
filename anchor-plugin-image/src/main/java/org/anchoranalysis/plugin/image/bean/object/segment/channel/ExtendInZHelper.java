/*-
 * #%L
 * anchor-image
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

package org.anchoranalysis.plugin.image.bean.object.segment.channel;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.voxel.BoundedVoxels;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.point.Point3i;
import org.anchoranalysis.spatial.point.ReadableTuple3i;

/**
 * Extends 2D objects as much as possible in z-dimension, while staying within a 3D binary mask.
 *
 * <p>TODO remove slices which have nothing in them from top and bottom
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ExtendInZHelper {

    public static ObjectCollection extendObjects(
            ObjectCollection objects2D, BinaryVoxels<UnsignedByteBuffer> mask3D) {
        return objects2D.stream().map(object -> extendObject(object, mask3D));
    }

    private static ObjectMask extendObject(
            ObjectMask object2D, BinaryVoxels<UnsignedByteBuffer> voxels3D) {
        return new ObjectMask(extendObject(object2D.boundedVoxels(), voxels3D));
    }

    private static BoundedVoxels<UnsignedByteBuffer> extendObject(
            BoundedVoxels<UnsignedByteBuffer> obj2D, BinaryVoxels<UnsignedByteBuffer> mask3D) {

        BoundingBox newBBox = createBoundingBoxForAllZ(obj2D.boundingBox(), mask3D.extent().z());

        BoundedVoxels<UnsignedByteBuffer> newMask =
                VoxelsFactory.getUnsignedByte().createBounded(newBBox);

        ReadableTuple3i max = newBBox.calculateCornerMaxInclusive();
        Point3i point = new Point3i();

        BinaryValuesByte binaryValues = mask3D.binaryValues().asByte();

        UnsignedByteBuffer bufferIn2D = obj2D.voxels().sliceBuffer(0);

        for (point.setZ(0); point.z() <= max.z(); point.incrementZ()) {

            UnsignedByteBuffer bufferMask3D = mask3D.voxels().sliceBuffer(point.z());
            UnsignedByteBuffer bufferOut3D = newMask.voxels().sliceBuffer(point.z());

            int offset = 0;

            for (point.setY(newBBox.cornerMin().y()); point.y() <= max.y(); point.incrementY()) {
                for (point.setX(newBBox.cornerMin().x());
                        point.x() <= max.x();
                        point.incrementX(), offset++) {

                    if (bufferIn2D.getRaw(offset) == binaryValues.getOn()) {
                        int indexGlobal = mask3D.extent().offset(point.x(), point.y());
                        bufferOut3D.putRaw(
                                offset,
                                bufferMask3D.getRaw(indexGlobal) == binaryValues.getOn()
                                        ? binaryValues.getOn()
                                        : binaryValues.getOff());
                    }
                }
            }
        }
        return newMask;
    }

    private static BoundingBox createBoundingBoxForAllZ(BoundingBox existing, int z) {
        Point3i cornerMin = copyPointChangeZ(existing.cornerMin(), 0);
        Extent extent = copyExtentChangeZ(existing.extent(), z);

        return BoundingBox.createReuse(cornerMin, extent);
    }

    private static Point3i copyPointChangeZ(ReadableTuple3i in, int z) {
        Point3i out = new Point3i(in);
        out.setZ(z);
        return out;
    }

    private static Extent copyExtentChangeZ(Extent in, int z) {
        return new Extent(in.x(), in.y(), z);
    }
}
