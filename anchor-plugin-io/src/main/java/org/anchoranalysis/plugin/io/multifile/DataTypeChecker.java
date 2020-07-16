/* (C)2020 */
package org.anchoranalysis.plugin.io.multifile;

import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;

/** Stores a data-type and checks all other slices match */
class DataTypeChecker {

    private VoxelDataType dataType = null; // The data type. All files must have the same.

    public void check(Stack stackForFile) throws RasterIOException {

        VoxelDataType stackDataType = stackForFile.getChnl(0).getVoxelDataType();
        if (!stackForFile.allChnlsHaveType(stackDataType)) {
            throw new RasterIOException("Channels have different data types");
        }

        checkSetDataType(stackDataType);
    }

    public VoxelDataType getDataType() {
        return dataType;
    }

    private void checkSetDataType(VoxelDataType stackDataType) throws RasterIOException {
        if (dataType == null) {
            // If first-time, then we record the data-type
            dataType = stackDataType;
        } else {
            // if subsequent time, then we check it matches
            if (!stackDataType.equals(dataType)) {
                throw new RasterIOException(
                        String.format(
                                "File has type %s. Other files have type %s",
                                stackDataType, dataType));
            }
        }
    }
}
