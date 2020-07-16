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
