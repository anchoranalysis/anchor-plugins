package org.anchoranalysis.plugin.io.bean.stack.writer.bioformats;

import org.anchoranalysis.image.io.RasterIOException;

/**
 * Creates or retrieves a byte representation of the voxels for a particular slice.
 * 
 * @author Owen Feehan
 *
 */
@FunctionalInterface
public interface ByteRepresentationForChannel {

    /**
     * The byte-representation of the voxels for a particular slice.
     * 
     * @param sliceIndex the index of the slice (z coordinate)
     * @return an existing (if possible preferably) or newly created byte-array representing a particular channel.
     * @throws RasterIOException if an unsupported source or destination data-type exists for conversion
     */
    public byte[] bytesForSlice(int sliceIndex) throws RasterIOException;
}
