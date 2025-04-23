/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.segment.watershed.encoding;

import lombok.AllArgsConstructor;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedIntBuffer;
import org.anchoranalysis.spatial.point.Point3i;

/**
 * A wrapper around {@link UnsignedIntBuffer} that provides methods for encoding and decoding
 * watershed-related information.
 */
@AllArgsConstructor
public final class EncodedIntBuffer {

    private final UnsignedIntBuffer delegate;
    private final WatershedEncoding encoding;

    /**
     * Checks if the value at the given offset is temporary.
     *
     * @param offset the offset in the buffer
     * @return true if the value is temporary, false otherwise
     */
    public boolean isTemporary(int offset) {
        return (delegate.getRaw(offset) == WatershedEncoding.CODE_TEMPORARY);
    }

    /**
     * Checks if the value at the given offset is unvisited.
     *
     * @param offset the offset in the buffer
     * @return true if the value is unvisited, false otherwise
     */
    public boolean isUnvisited(int offset) {
        return (delegate.getRaw(offset) == WatershedEncoding.CODE_UNVISITED);
    }

    /**
     * Checks if the value at the given offset is a minima.
     *
     * @param offset the offset in the buffer
     * @return true if the value is a minima, false otherwise
     */
    public boolean isMinima(int offset) {
        return (delegate.getRaw(offset) == WatershedEncoding.CODE_MINIMA);
    }

    /**
     * Checks if the value at the given offset is a connected component ID.
     *
     * @param offset the offset in the buffer
     * @return true if the value is a connected component ID, false otherwise
     */
    public boolean isConnectedComponentID(int offset) {
        return (encoding.isConnectedComponentIDCode(delegate.getRaw(offset)));
    }

    /**
     * Marks the value at the given offset as temporary.
     *
     * @param offset the offset in the buffer
     */
    public void markAsTemporary(int offset) {
        delegate.putRaw(offset, WatershedEncoding.CODE_TEMPORARY);
    }

    /**
     * Gets the encoded value at the given index.
     *
     * @param index the index in the buffer
     * @return the encoded value
     */
    public int getCode(int index) {
        return delegate.getRaw(index);
    }

    /**
     * Puts an encoded value at the given index.
     *
     * @param index the index in the buffer
     * @param code the encoded value to put
     */
    public void putCode(int index, int code) {
        delegate.putRaw(index, code);
    }

    /**
     * Converts the code at the given index to a connected component ID.
     *
     * @param indexBuffer the index in this buffer
     * @param indexGlobal the global index
     * @param matS the {@link EncodedVoxels} containing the watershed encoding
     * @param point the {@link Point3i} corresponding to the current position
     */
    public void convertCode(int indexBuffer, int indexGlobal, EncodedVoxels matS, Point3i point) {
        int currentValue = getCode(indexBuffer);

        assert (!matS.isPlateau(currentValue)); // NOSONAR
        assert (!matS.isUnvisited(currentValue)); // NOSONAR
        assert (!matS.isTemporary(currentValue)); // NOSONAR

        // We translate the value into directions and use that to determine where to
        //   travel to
        if (matS.isMinima(currentValue)) {

            putConnectedComponentID(indexBuffer, indexGlobal);

            // We maintain a mapping between each minimas indxGlobal and
        } else if (matS.isConnectedComponentIDCode(currentValue)) {
            // NO CHANGE
        } else {
            int finalIndex = matS.calculateConnectedComponentID(point, currentValue);
            putCode(indexBuffer, finalIndex);
        }
    }

    /**
     * Puts an encoded connected component ID at the given index.
     *
     * @param index the index in the buffer
     * @param connectedComponentID the connected component ID to encode and put
     */
    private void putConnectedComponentID(int index, int connectedComponentID) {
        int encoded = encoding.encodeConnectedComponentID(connectedComponentID);
        delegate.putRaw(index, encoded);
    }
}
