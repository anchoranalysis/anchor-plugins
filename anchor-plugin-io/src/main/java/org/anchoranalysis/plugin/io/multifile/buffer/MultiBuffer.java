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
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
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

    public void populateWithSpecifiedChnl(
            Stack stackForFile, int chnlNum, Optional<Integer> sliceNum, int timeIndex) {
        // Specific Channel Number, but no specific Slice Number
        Channel chnl = stackForFile.getChannel(0);
        Voxels<?> voxels = chnl.voxels().any();

        int chnlIndexResolved = size.getRangeC().index(chnlNum);
        int timeIndexResolved = size.getRangeT().index(timeIndex);

        if (sliceNum.isPresent()) {
            copyFirstSliceForChnl(timeIndexResolved, chnlIndexResolved, voxels, sliceNum.get());

        } else {
            copyAllSlicesForChnl(timeIndexResolved, chnlIndexResolved, voxels);
        }
    }

    public void populateWithSpecifiedSlice(Stack stackForFile, int sliceNum, int timeIndex) {

        int timeIndexResolved = size.getRangeT().index(timeIndex);

        for (int c = 0; c < stackForFile.getNumberChannels(); c++) {
            Channel chnl = stackForFile.getChannel(c);
            copyFirstSliceForChnl(timeIndexResolved, c, chnl.voxels().any(), sliceNum);
        }
    }

    public void populateNoSpecifics(Stack stackForFile, int timeIndex) {

        int timeIndexResolved = size.getRangeT().index(timeIndex);

        // No specific Channel Number, and no specific Slice Number
        // Then we have to guess the channel
        for (int c = 0; c < stackForFile.getNumberChannels(); c++) {
            Channel chnl = stackForFile.getChannel(c);
            copyAllSlicesForChnl(timeIndexResolved, c, chnl.voxels().any());
        }
    }

    public Stack createStackForIndex(int t, ImageDimensions dimensions, VoxelDataType dataType) {

        Stack stack = new Stack();

        for (int c = 0; c < size.getRangeC().getSize(); c++) {

            Channel chnl = ChannelFactory.instance().createEmptyUninitialised(dimensions, dataType);
            copyAllBuffersTo(t, c, chnl.voxels());

            try {
                stack.addChannel(chnl);
            } catch (IncorrectImageSizeException e) {
                assert false;
            }
        }
        return stack;
    }

    @SuppressWarnings("unchecked")
    private void copyAllBuffersTo(int t, int c, VoxelsWrapper voxels) {
        for (int z = 0; z < size.getRangeZ().getSize(); z++) {
            voxels.any().updateSlice(z, buffers[t][c][z]);
        }
    }

    private void copyFirstSliceForChnl(int t, int c, Voxels<?> voxels, int sliceNum) {
        buffers[t][c][size.getRangeZ().index(sliceNum)] = voxels.slice(0);
    }

    private void copyAllSlicesForChnl(int t, int c, Voxels<?> voxels) {
        for (int z = 0; z < voxels.extent().z(); z++) {
            buffers[t][c][z] = voxels.slice(z);
        }
    }
}
