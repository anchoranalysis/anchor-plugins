/*-
 * #%L
 * anchor-plugin-opencv
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
package org.anchoranalysis.plugin.opencv.convert;

import java.nio.FloatBuffer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedShortBuffer;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.spatial.box.Extent;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class VoxelsFromMat {

    public static Channel toChannel(Mat mat, Extent extent) throws OperationFailedException {
        Voxels<?> voxels = VoxelsFromMat.toVoxels(mat, extent);
        return ChannelFactory.instance().create(voxels);
    }

    public static Voxels<?> toVoxels( // NOSONAR
            Mat mat, Extent extent) throws OperationFailedException {

        if (mat.depth() == CvType.CV_8U) {
            return unsignedByteFromMat(mat, extent);

        } else if (mat.depth() == CvType.CV_16U) {
            return unsignedShortFromMat(mat, extent);

        } else if (mat.depth() == CvType.CV_32F) {
            return floatFromMat(mat, extent);
        } else {
            throw new OperationFailedException("Unsupported OpenCV type: " + mat.depth());
        }
    }

    private static Voxels<UnsignedByteBuffer> unsignedByteFromMat(Mat mat, Extent extent) {
        VoxelBuffer<UnsignedByteBuffer> buffer =
                VoxelBufferFromMat.unsignedByteFromMat(mat, extent);
        return VoxelsFactory.getUnsignedByte().createForVoxelBuffer(buffer, extent);
    }

    private static Voxels<UnsignedShortBuffer> unsignedShortFromMat(Mat mat, Extent extent) {
        VoxelBuffer<UnsignedShortBuffer> buffer =
                VoxelBufferFromMat.unsignedShortFromMat(mat, extent);
        return VoxelsFactory.getUnsignedShort().createForVoxelBuffer(buffer, extent);
    }

    private static Voxels<FloatBuffer> floatFromMat(Mat mat, Extent extent) {
        VoxelBuffer<FloatBuffer> buffer = VoxelBufferFromMat.floatFromMat(mat, extent);
        return VoxelsFactory.getFloat().createForVoxelBuffer(buffer, extent);
    }
}
