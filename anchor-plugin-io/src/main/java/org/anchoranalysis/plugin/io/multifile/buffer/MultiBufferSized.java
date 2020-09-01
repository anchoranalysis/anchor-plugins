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
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.TimeSequence;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.plugin.io.multifile.SizeExtents;

public class MultiBufferSized {

    private Dimensions dimensions;
    private int sizeT;
    private MultiBuffer buffers;

    public MultiBufferSized(Stack stack, SizeExtents size) {
        buffers = new MultiBuffer(stack, size);
        dimensions = new Dimensions(size.toExtent(), stack.dimensions().resolution());
        sizeT = size.getRangeT().getSize();
    }

    public void populateFrom(
            Stack stackForFile,
            Optional<Integer> channelNum,
            Optional<Integer> sliceNum,
            Optional<Integer> timeIndex) {

        // If timeIndex is unspecified, we assume not
        int time = timeIndex.orElse(0);

        // If we specify a channel, then we only care about slices
        if (channelNum.isPresent()) {
            buffers.populateWithSpecifiedChannel(stackForFile, channelNum.get(), sliceNum, time);
        } else {

            if (sliceNum.isPresent()) {
                // No specific Channel Number, but specific Slice Number
                buffers.populateWithSpecifiedSlice(stackForFile, sliceNum.get(), time);

            } else {
                buffers.populateNoSpecifics(stackForFile, time);
            }
        }
    }

    public TimeSequence createSequence(VoxelDataType dataType) {
        TimeSequence tsOut = new TimeSequence();
        for (int t = 0; t < sizeT; t++) {
            tsOut.add(buffers.createStackForIndex(t, dimensions, dataType));
        }
        return tsOut;
    }
}
