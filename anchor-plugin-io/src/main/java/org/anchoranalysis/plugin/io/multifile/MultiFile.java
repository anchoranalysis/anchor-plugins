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

package org.anchoranalysis.plugin.io.multifile;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.TimeSequence;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.plugin.io.multifile.buffer.MultiBufferSized;

// We try to guess these parameters from the fileBag, and if not, from after we add the first image
class MultiFile {

    private SizeExtents size;

    private DataTypeChecker dataTypeChecker = new DataTypeChecker();

    // What we populate before creating the final stack, buffers is sorted by channel and then by
    // slice
    private MultiBufferSized buffers;

    public MultiFile(ParsedFilePathBag fileBag) {
        super();
        size = new SizeExtents(fileBag);
    }

    public void add(
            Stack stackForFile,
            Optional<Integer> channelNum,
            Optional<Integer> sliceNum,
            Optional<Integer> timeIndex,
            Path filePath)
            throws RasterIOException {

        dataTypeChecker.check(stackForFile);

        if (buffers == null) {
            buffers = new MultiBufferSized(stackForFile, size);
        }
        assert (size.hasNecessaryExtents());

        checkChannelNum(stackForFile, channelNum, filePath);
        checkSliceNum(stackForFile, sliceNum, filePath);

        buffers.populateFrom(stackForFile, channelNum, sliceNum, timeIndex);
    }

    public TimeSequence createSequence() {
        return buffers.createSequence(dataTypeChecker.getDataType());
    }

    private void checkSliceNum(Stack stackForFile, Optional<Integer> sliceNum, Path filePath)
            throws RasterIOException {
        if (sliceNum.isPresent()) {
            if (stackForFile.dimensions().z() != 1) {
                throw new RasterIOException(
                        String.format(
                                "A sliceNum %d is specified, but the file '%s' has more than one slice",
                                sliceNum.get(), filePath));
            }
        } else {
            if (stackForFile.dimensions().z() != size.getRangeZ().getSize()) {
                throw new RasterIOException(
                        String.format("File '%s' has an incorrect number of slices", filePath));
            }
        }
    }

    private void checkChannelNum(Stack stackForFile, Optional<Integer> channelNum, Path filePath)
            throws RasterIOException {
        if (channelNum.isPresent()) {
            if (stackForFile.getNumberChannels() != 1) {
                throw new RasterIOException(
                        String.format(
                                "A channelNum %d is specified, but the file '%s' has more than one channel",
                                channelNum.get(), filePath));
            }
        } else {
            if (stackForFile.getNumberChannels() != size.getRangeC().getSize()) {
                throw new RasterIOException(
                        String.format("File '%s' has an incorrect number of channels", filePath));
            }
        }
    }

    public int numChannel() {
        return size.getRangeC().getSize();
    }

    public int numFrames() {
        return size.getRangeT().getSize();
    }

    public boolean numFramesDefined() {
        return size.rangeTPresent();
    }

    public boolean numChannelDefined() {
        return size.rangeCPresent();
    }

    public VoxelDataType dataType() {
        return dataTypeChecker.getDataType();
    }
}
