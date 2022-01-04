/*-
 * #%L
 * anchor-plugin-onnx
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.onnx.bean.object.segment.stack;

import java.nio.FloatBuffer;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Creates a {@link FloatBuffer} from a {@link Stack}, optionally subtracting channel-means.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class BufferFromStack {

    /**
     * Creates a {@link FloatBuffer} representation of a {@link Stack}.
     *
     * @param stack the stack whose voxels will be converted into the {@link FloatBuffer}.
     * @param subtractMeans respective intensity values that are subtracted from the voxels before
     *     being added to the tensor (respectively for each channel).
     * @param interleaveChannels if true, the channels are placed as the final position of the
     *     tensor (**after** width/height) instead of **before** width/height.
     * @return a newly created buffer that is a representation of {@code stack}, as per above.
     * @throws OperationFailedException if any channel is of unsupported data-type, or if parameters
     *     are in an illegal state.
     */
    public static FloatBuffer createFrom(
            Stack stack, Optional<double[]> subtractMeans, boolean interleaveChannels)
            throws OperationFailedException {

        checkChannels(stack);

        Dimensions dimensions = stack.getChannel(0).dimensions();

        FloatBuffer buffer =
                FloatBuffer.allocate(
                        (int) dimensions.calculateVolume() * stack.getNumberChannels());

        if (subtractMeans.isPresent() && subtractMeans.get().length != stack.getNumberChannels()) {
            throw new OperationFailedException(
                    String.format(
                            "subtractMeans has size=%d whereas the stack has %d channels. They must be equal.",
                            subtractMeans.get().length, stack.getNumberChannels()));
        }

        if (interleaveChannels) {
            copyChannelLast(buffer, stack, subtractMeans);
        } else {
            copyChannelFirst(buffer, stack, subtractMeans);
        }

        return buffer;
    }

    private static void checkChannels(Stack stack) throws OperationFailedException {
        for (int channelIndex = 0; channelIndex < stack.getNumberChannels(); channelIndex++) {
            if (stack.getChannel(channelIndex).getVoxelDataType()
                    != UnsignedByteVoxelType.INSTANCE) {
                throw new OperationFailedException(
                        String.format(
                                "Only unsigned-byte channels are supported for conversion into an ONNX Runtime tensor, but channel %d is %s",
                                channelIndex, stack.getChannel(channelIndex).getVoxelDataType()));
            }
        }
    }

    /**
     * Copies voxels into {@link FloatBuffer} in the order Channels, Height, Width i.e. no
     * interleaving of channels.
     */
    private static void copyChannelFirst(
            FloatBuffer buffer, Stack stack, Optional<double[]> subtractMeans) {
        for (int channelIndex = 0; channelIndex < stack.getNumberChannels(); channelIndex++) {

            Channel channel = stack.getChannel(channelIndex);

            float valueToRemove =
                    subtractMeans.isPresent() ? (float) subtractMeans.get()[channelIndex] : 0.0f;
            copyChannelIntoBuffer(channel.voxels().asByte(), buffer, valueToRemove);
        }
    }

    private static float[] convertToFloat(double[] values) {
        return new float[] {(float) values[0], (float) values[1], (float) values[2]};
    }

    private static void updateBuffers(Stack stack, int z, UnsignedByteBuffer[] sliceBuffers) {
        for (int channelIndex = 0; channelIndex < stack.getNumberChannels(); channelIndex++) {
            UnsignedByteBuffer buffer =
                    stack.getChannel(channelIndex).voxels().asByte().slice(z).buffer();
            buffer.rewind();
            sliceBuffers[channelIndex] = buffer;
        }
    }

    /**
     * Copy the voxels for a single channel into {@code destination} (whose relative put methods are
     * employed).
     */
    private static void copyChannelIntoBuffer(
            Voxels<UnsignedByteBuffer> voxels, FloatBuffer destination, float valueToRemove) {

        Extent extent = voxels.extent();
        for (int z = 0; z < extent.z(); z++) {
            UnsignedByteBuffer buffer = voxels.sliceBuffer(z);
            int index = 0;
            while (index < extent.areaXY()) {
                float value = buffer.getUnsigned();
                destination.put(value - valueToRemove);
                index++;
            }
        }
    }

    /**
     * Copies voxels into {@link FloatBuffer} in the order Height, Width, Channels i.e. with
     * interleaving of channels.
     */
    private static void copyChannelLast(
            FloatBuffer buffer, Stack stack, Optional<double[]> subtractMeans) {

        float[] valuesToRemove =
                subtractMeans.isPresent() ? convertToFloat(subtractMeans.get()) : new float[3];

        UnsignedByteBuffer[] sliceBuffers = new UnsignedByteBuffer[stack.getNumberChannels()];

        Extent extent = stack.getChannel(0).extent();
        for (int z = 0; z < extent.z(); z++) {

            updateBuffers(stack, z, sliceBuffers);

            int index = 0;

            while (index < extent.areaXY()) {

                for (int channelIndex = 0;
                        channelIndex < stack.getNumberChannels();
                        channelIndex++) {
                    float value = sliceBuffers[channelIndex].getUnsigned();
                    buffer.put(value - valuesToRemove[channelIndex]);
                }

                index++;
            }
        }
    }
}
