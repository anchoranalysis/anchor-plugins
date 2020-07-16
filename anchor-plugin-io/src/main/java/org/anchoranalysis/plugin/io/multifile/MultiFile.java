/* (C)2020 */
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

    // What we populate before creating the final stack, buffers is sorted by chnl and then by slice
    private MultiBufferSized buffers;

    public MultiFile(ParsedFilePathBag fileBag) {
        super();
        size = new SizeExtents(fileBag);
    }

    public void add(
            Stack stackForFile,
            Optional<Integer> chnlNum,
            Optional<Integer> sliceNum,
            Optional<Integer> timeIndex,
            Path filePath)
            throws RasterIOException {

        dataTypeChecker.check(stackForFile);

        if (buffers == null) {
            buffers = new MultiBufferSized(stackForFile, size);
        }
        assert (size.hasNecessaryExtents());

        checkChnlNum(stackForFile, chnlNum, filePath);
        checkSliceNum(stackForFile, sliceNum, filePath);

        buffers.populateFrom(stackForFile, chnlNum, sliceNum, timeIndex);
    }

    public TimeSequence createSequence() {
        return buffers.createSequence(dataTypeChecker.getDataType());
    }

    private void checkSliceNum(Stack stackForFile, Optional<Integer> sliceNum, Path filePath)
            throws RasterIOException {
        if (sliceNum.isPresent()) {
            if (stackForFile.getDimensions().getZ() != 1) {
                throw new RasterIOException(
                        String.format(
                                "A sliceNum %d is specified, but the file '%s' has more than one slice",
                                sliceNum.get(), filePath));
            }
        } else {
            if (stackForFile.getDimensions().getZ() != size.getRangeZ().getSize()) {
                throw new RasterIOException(
                        String.format("File '%s' has an incorrect number of slices", filePath));
            }
        }
    }

    private void checkChnlNum(Stack stackForFile, Optional<Integer> chnlNum, Path filePath)
            throws RasterIOException {
        if (chnlNum.isPresent()) {
            if (stackForFile.getNumChnl() != 1) {
                throw new RasterIOException(
                        String.format(
                                "A chnlNum %d is specified, but the file '%s' has more than one channel",
                                chnlNum.get(), filePath));
            }
        } else {
            if (stackForFile.getNumChnl() != size.getRangeC().getSize()) {
                throw new RasterIOException(
                        String.format("File '%s' has an incorrect number of channels", filePath));
            }
        }
    }

    public int numChnl() {
        return size.getRangeC().getSize();
    }

    public int numFrames() {
        return size.getRangeT().getSize();
    }

    public boolean numFramesDefined() {
        return size.rangeTPresent();
    }

    public boolean numChnlDefined() {
        return size.rangeCPresent();
    }

    public VoxelDataType dataType() {
        return dataTypeChecker.getDataType();
    }
}
