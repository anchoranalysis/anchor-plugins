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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.convert.UnsignedBuffer;
import org.anchoranalysis.image.convert.UnsignedBufferAsInt;
import org.anchoranalysis.image.convert.UnsignedIntBuffer;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;

/**
 * Performs arithmetic operations on corresponding voxels from two {@link Voxels}.
 *
 * <p>The arithmetic operation is indepently applied to each voxel location independently.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class VoxelwiseArithmetic {

    public static void divide(Voxels<UnsignedIntBuffer> voxels, int count, VoxelsWrapper out)
            throws OperationFailedException {

        VoxelDataType outputType = out.getVoxelDataType();

        if (outputType.equals(UnsignedShortVoxelType.INSTANCE)) {
            divideValue(voxels, count, out.asShort());
        } else if (outputType.equals(UnsignedByteVoxelType.INSTANCE)) {
            divideValue(voxels, count, out.asByte());
        } else {
            throwUnsupportedDataTypeException(outputType);
        }
    }

    public static void add(Voxels<UnsignedIntBuffer> voxels, VoxelsWrapper toAdd)
            throws OperationFailedException {

        VoxelDataType toAddType = toAdd.getVoxelDataType();

        if (toAddType.equals(UnsignedShortVoxelType.INSTANCE)) {
            add(voxels, toAdd.asShort());
        } else if (toAddType.equals(UnsignedByteVoxelType.INSTANCE)) {
            add(voxels, toAdd.asByte());
        } else {
            throwUnsupportedDataTypeException(toAddType);
        }
    }

    /**
     * Adds each voxel in {@code toAdd} to the corresponding existing voxel in {@code voxels}.
     *
     * <p>The output of the addition is wrotten back onto {@code voxels}.
     *
     * @param <T> buffer-type of {@code toAdd}.
     * @param voxels voxels to add to
     * @param toAdd voxels that are added.
     */
    private static <T extends UnsignedBufferAsInt> void add(
            Voxels<UnsignedIntBuffer> voxels, Voxels<T> toAdd) {

        toAdd.extent()
                .iterateOverZ(
                        z -> {
                            UnsignedIntBuffer buffer1 = voxels.sliceBuffer(z);
                            T buffer2 = toAdd.sliceBuffer(z);

                            while (buffer1.hasRemaining()) {
                                long sum = buffer1.getUnsigned() + buffer2.getUnsigned();
                                oneStepBackward(buffer1);
                                buffer1.putUnsigned(sum);
                            }
                        });
    }

    /**
     * Divides each voxel value in {@code voxelsIn} by the constant div, and places result in {@code
     * voxelsOut}.
     *
     * @param in voxels to divide
     * @param divideBy what to divide by
     * @param out voxels where output is placed
     * @param <T> buffer-type of {@code voxelsOut}
     */
    private static <T extends UnsignedBuffer> void divideValue(
            Voxels<UnsignedIntBuffer> in, int divideBy, Voxels<T> out) {

        in.extent()
                .iterateOverZ(
                        z -> {
                            UnsignedIntBuffer bufferIn = in.sliceBuffer(z);
                            T bufferOut = out.sliceBuffer(z);

                            while (bufferIn.hasRemaining()) {
                                bufferOut.putLong(bufferIn.getUnsigned() / divideBy);
                            }
                        });
    }

    private static void oneStepBackward(UnsignedIntBuffer buffer) {
        buffer.position(buffer.position() - 1);
    }

    private static void throwUnsupportedDataTypeException(VoxelDataType voxelDataType)
            throws OperationFailedException {
        throw new OperationFailedException(
                String.format("Unsupported data type: %s", voxelDataType));
    }
}
