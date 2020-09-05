/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean.grouped.raster;

import org.anchoranalysis.image.convert.UnsignedByteBuffer;
import org.anchoranalysis.image.convert.UnsignedShortBuffer;
import org.anchoranalysis.image.convert.UnsignedIntBuffer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class VoxelsArithmetic {

    public static void divide(
            Voxels<UnsignedIntBuffer> voxels, int cnt, VoxelsWrapper out, VoxelDataType outputType)
            throws OperationFailedException {

        if (outputType.equals(UnsignedShortVoxelType.INSTANCE)) {
            divideValueShort(voxels, cnt, out.asShort());
        } else if (outputType.equals(UnsignedByteVoxelType.INSTANCE)) {
            divideValueByte(voxels, cnt, out.asByte());
        } else {
            throwUnsupportedDataTypeException(outputType);
            // never reached
            assert (false);
        }
    }

    public static void add(Voxels<UnsignedIntBuffer> voxels, VoxelsWrapper toAdd, VoxelDataType toAddType)
            throws OperationFailedException {

        if (toAddType.equals(UnsignedShortVoxelType.INSTANCE)) {
            addShort(voxels, toAdd.asShort());
        } else if (toAddType.equals(UnsignedByteVoxelType.INSTANCE)) {
            addByte(voxels, toAdd.asByte());
        } else {
            throwUnsupportedDataTypeException(toAddType);
        }
    }

    /**
     * Divides each voxel value in src by the constant div, and places result in out
     *
     * @param voxelsIn
     * @param div
     * @param voxelsOut
     */
    private static void divideValueShort(
            Voxels<UnsignedIntBuffer> voxelsIn, int div, Voxels<UnsignedShortBuffer> voxelsOut) {

        voxelsIn.extent().iterateOverZ( z-> {

            UnsignedIntBuffer in = voxelsIn.sliceBuffer(z);
            UnsignedShortBuffer out = voxelsOut.sliceBuffer(z);

            while (in.hasRemaining()) {
                out.putLong(in.getUnsigned() / div);
            }

            assert (!in.hasRemaining());
            assert (!out.hasRemaining());
        });
    }

    /**
     * Divides each voxel value in src by the constant div, and places result in out
     *
     * @param voxelsIn
     * @param div
     * @param voxelsOut
     */
    private static void divideValueByte(
            Voxels<UnsignedIntBuffer> voxelsIn, int div, Voxels<UnsignedByteBuffer> voxelsOut) {

        for (int z = 0; z < voxelsIn.extent().z(); z++) {

            UnsignedIntBuffer in = voxelsIn.sliceBuffer(z);
            UnsignedByteBuffer out = voxelsOut.sliceBuffer(z);

            while (in.hasRemaining()) {
                out.putLong(in.getUnsigned() / div);
            }

            assert (!in.hasRemaining());
            assert (!out.hasRemaining());
        }
    }

    private static void throwUnsupportedDataTypeException(VoxelDataType voxelDataType)
            throws OperationFailedException {
        throw new OperationFailedException(
                String.format("Unsupported data type: %s", voxelDataType));
    }

    private static void addShort(Voxels<UnsignedIntBuffer> voxels, Voxels<UnsignedShortBuffer> toAdd) {

        for (int z = 0; z < toAdd.extent().z(); z++) {

            UnsignedIntBuffer in1 = voxels.sliceBuffer(z);
            UnsignedShortBuffer in2 = toAdd.sliceBuffer(z);

            while (in1.hasRemaining()) {
                long sum = in1.getUnsigned() + in2.getUnsigned();
                oneStepBackward(in1);
                in1.putUnsigned(sum);
            }

            assert (!in1.hasRemaining());
            assert (!in2.hasRemaining());
        }
    }

    private static void addByte(Voxels<UnsignedIntBuffer> voxels, Voxels<UnsignedByteBuffer> toAdd) {

        for (int z = 0; z < toAdd.extent().z(); z++) {

            UnsignedIntBuffer in1 = voxels.sliceBuffer(z);
            UnsignedByteBuffer in2 = toAdd.sliceBuffer(z);

            while (in1.hasRemaining()) {
                long sum = in1.getUnsigned() + in2.getUnsigned();
                oneStepBackward(in1);
                in1.putUnsigned(sum);
            }

            assert (!in1.hasRemaining());
            assert (!in2.hasRemaining());
        }
    }

    private static void oneStepBackward(UnsignedIntBuffer buffer) {
        buffer.position(buffer.position() - 1);
    }
}
