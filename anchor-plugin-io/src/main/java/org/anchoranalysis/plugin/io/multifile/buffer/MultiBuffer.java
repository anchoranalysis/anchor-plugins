/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.multifile.buffer;

import java.util.Optional;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsUntyped;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.plugin.io.multifile.SizeExtents;

class MultiBuffer {

    // Time, Channel, Slices
    @SuppressWarnings("rawtypes")
    private VoxelBuffer[][][] buffers;

    private SizeExtents size;

    public MultiBuffer(Stack stackArbitrarySlice, SizeExtents size) {
        this.size = size;

        size.populateMissingFromArbitrarySlice(stackArbitrarySlice);
        buffers =
                new VoxelBuffer<?>[size.getRangeT().getSize()][size.getRangeC().getSize()]
                        [size.getRangeZ().getSize()];
    }

    public void populateWithSpecifiedChannel(
            Stack stackForFile, int channelNum, Optional<Integer> sliceNum, int timeIndex) {
        // Specific Channel Number, but no specific Slice Number
        Channel channel = stackForFile.getChannel(0);
        Voxels<?> voxels = channel.voxels().any();

        int channelIndexResolved = size.getRangeC().index(channelNum);
        int timeIndexResolved = size.getRangeT().index(timeIndex);

        if (sliceNum.isPresent()) {
            copyFirstSliceForChannel(
                    timeIndexResolved, channelIndexResolved, voxels, sliceNum.get());

        } else {
            copyAllSlicesForChannel(timeIndexResolved, channelIndexResolved, voxels);
        }
    }

    public void populateWithSpecifiedSlice(Stack stackForFile, int sliceNum, int timeIndex) {

        int timeIndexResolved = size.getRangeT().index(timeIndex);

        for (int c = 0; c < stackForFile.getNumberChannels(); c++) {
            Channel channel = stackForFile.getChannel(c);
            copyFirstSliceForChannel(timeIndexResolved, c, channel.voxels().any(), sliceNum);
        }
    }

    public void populateNoSpecifics(Stack stackForFile, int timeIndex) {

        int timeIndexResolved = size.getRangeT().index(timeIndex);

        // No specific Channel Number, and no specific Slice Number
        // Then we have to guess the channel
        for (int c = 0; c < stackForFile.getNumberChannels(); c++) {
            Channel channel = stackForFile.getChannel(c);
            copyAllSlicesForChannel(timeIndexResolved, c, channel.voxels().any());
        }
    }

    public Stack createStackForIndex(int t, Dimensions dimensions, VoxelDataType dataType) {

        Stack stack = new Stack();

        for (int c = 0; c < size.getRangeC().getSize(); c++) {

            Channel channel = ChannelFactory.instance().createUninitialised(dimensions, dataType);
            copyAllBuffersTo(t, c, channel.voxels());

            try {
                stack.addChannel(channel);
            } catch (IncorrectImageSizeException e) {
                assert false;
            }
        }
        return stack;
    }

    @SuppressWarnings("unchecked")
    private void copyAllBuffersTo(int t, int c, VoxelsUntyped voxels) {
        for (int z = 0; z < size.getRangeZ().getSize(); z++) {
            voxels.any().replaceSlice(z, buffers[t][c][z]);
        }
    }

    private void copyFirstSliceForChannel(int t, int c, Voxels<?> voxels, int sliceNum) {
        buffers[t][c][size.getRangeZ().index(sliceNum)] = voxels.slice(0);
    }

    private void copyAllSlicesForChannel(int t, int c, Voxels<?> voxels) {
        for (int z = 0; z < voxels.extent().z(); z++) {
            buffers[t][c][z] = voxels.slice(z);
        }
    }
}
