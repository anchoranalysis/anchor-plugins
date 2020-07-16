/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.grouped.raster;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedShort;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class VoxelBoxArithmetic {

    public static void divide(
            VoxelBox<IntBuffer> vb, int cnt, VoxelBoxWrapper out, VoxelDataType outputType)
            throws OperationFailedException {

        if (outputType.equals(VoxelDataTypeUnsignedShort.INSTANCE)) {
            divideValueShort(vb, cnt, out.asShort());
        } else if (outputType.equals(VoxelDataTypeUnsignedByte.INSTANCE)) {
            divideValueByte(vb, cnt, out.asByte());
        } else {
            throwUnsupportedDataTypeException(outputType);
            // never reached
            assert (false);
        }
    }

    public static void add(VoxelBox<IntBuffer> vb, VoxelBoxWrapper toAdd, VoxelDataType toAddType)
            throws OperationFailedException {

        if (toAddType.equals(VoxelDataTypeUnsignedShort.INSTANCE)) {
            addShort(vb, toAdd.asShort());
        } else if (toAddType.equals(VoxelDataTypeUnsignedByte.INSTANCE)) {
            addByte(vb, toAdd.asByte());
        } else {
            throwUnsupportedDataTypeException(toAddType);
        }
    }

    /**
     * Divides each voxel value in src by the constant div, and places result in out
     *
     * @param vbIn
     * @param div
     * @param vbOut
     */
    private static void divideValueShort(
            VoxelBox<IntBuffer> vbIn, int div, VoxelBox<ShortBuffer> vbOut) {

        for (int z = 0; z < vbIn.extent().getZ(); z++) {

            IntBuffer in = vbIn.getPixelsForPlane(z).buffer();
            ShortBuffer out = vbOut.getPixelsForPlane(z).buffer();

            while (in.hasRemaining()) {

                int b1 = in.get();
                out.put((short) (b1 / div));
            }

            assert (!in.hasRemaining());
            assert (!out.hasRemaining());
        }
    }

    /**
     * Divides each voxel value in src by the constant div, and places result in out
     *
     * @param vbIn
     * @param div
     * @param vbOut
     */
    private static void divideValueByte(
            VoxelBox<IntBuffer> vbIn, int div, VoxelBox<ByteBuffer> vbOut) {

        for (int z = 0; z < vbIn.extent().getZ(); z++) {

            IntBuffer in = vbIn.getPixelsForPlane(z).buffer();
            ByteBuffer out = vbOut.getPixelsForPlane(z).buffer();

            while (in.hasRemaining()) {

                int b1 = in.get();
                out.put((byte) (b1 / div));
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

    private static void addShort(VoxelBox<IntBuffer> vb, VoxelBox<ShortBuffer> toAdd) {

        for (int z = 0; z < toAdd.extent().getZ(); z++) {

            IntBuffer in1 = vb.getPixelsForPlane(z).buffer();
            ShortBuffer in2 = toAdd.getPixelsForPlane(z).buffer();

            while (in1.hasRemaining()) {

                int b1 = in1.get();
                short b2 = in2.get();

                int sum = b1 + ByteConverter.unsignedShortToInt(b2);
                oneStepBackward(in1);
                in1.put(sum);
            }

            assert (!in1.hasRemaining());
            assert (!in2.hasRemaining());
        }
    }

    private static void addByte(VoxelBox<IntBuffer> vb, VoxelBox<ByteBuffer> toAdd) {

        for (int z = 0; z < toAdd.extent().getZ(); z++) {

            IntBuffer in1 = vb.getPixelsForPlane(z).buffer();
            ByteBuffer in2 = toAdd.getPixelsForPlane(z).buffer();

            while (in1.hasRemaining()) {

                int b1 = in1.get();
                byte b2 = in2.get();

                int sum = b1 + ByteConverter.unsignedByteToInt(b2);
                oneStepBackward(in1);
                in1.put(sum);
            }

            assert (!in1.hasRemaining());
            assert (!in2.hasRemaining());
        }
    }

    private static void oneStepBackward(Buffer buffer) {
        buffer.position(buffer.position() - 1);
    }
}
