/* (C)2020 */
package org.anchoranalysis.plugin.io.multifile.buffer;

import java.util.Optional;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.TimeSequence;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.plugin.io.multifile.SizeExtents;

public class MultiBufferSized {

    private ImageDimensions dimensions;
    private int sizeT;
    private MultiBuffer buffers;

    public MultiBufferSized(Stack stack, SizeExtents size) {
        buffers = new MultiBuffer(stack, size);
        dimensions = new ImageDimensions(size.toExtent(), stack.getDimensions().getRes());
        sizeT = size.getRangeT().getSize();
    }

    public void populateFrom(
            Stack stackForFile,
            Optional<Integer> chnlNum,
            Optional<Integer> sliceNum,
            Optional<Integer> timeIndex) {

        // If timeIndex is unspecified, we assume not
        int time = timeIndex.orElse(0);

        // If we specify a channel, then we only care about slices
        if (chnlNum.isPresent()) {
            buffers.populateWithSpecifiedChnl(stackForFile, chnlNum.get(), sliceNum, time);
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
