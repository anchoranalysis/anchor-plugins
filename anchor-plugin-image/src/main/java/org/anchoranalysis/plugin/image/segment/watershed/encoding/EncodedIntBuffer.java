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

@AllArgsConstructor
public final class EncodedIntBuffer {

    private final UnsignedIntBuffer delegate;
    private final WatershedEncoding encoding;

    public boolean isTemporary(int offset) {
        return (delegate.getRaw(offset) == WatershedEncoding.CODE_TEMPORARY);
    }

    public boolean isUnvisited(int offset) {
        return (delegate.getRaw(offset) == WatershedEncoding.CODE_UNVISITED);
    }

    public boolean isMinima(int offset) {
        return (delegate.getRaw(offset) == WatershedEncoding.CODE_MINIMA);
    }

    public boolean isConnectedComponentID(int offset) {
        return (encoding.isConnectedComponentIDCode(delegate.getRaw(offset)));
    }

    public void markAsTemporary(int offset) {
        delegate.putRaw(offset, WatershedEncoding.CODE_TEMPORARY);
    }

    public int getCode(int index) {
        return delegate.getRaw(index);
    }

    public void putCode(int index, int code) {
        delegate.putRaw(index, code);
    }

    /** Convert code to connected-component */
    public void convertCode(int indxBuffer, int indxGlobal, EncodedVoxels matS, Point3i point) {
        int crntVal = getCode(indxBuffer);

        assert (!matS.isPlateau(crntVal)); // NOSONAR
        assert (!matS.isUnvisited(crntVal)); // NOSONAR
        assert (!matS.isTemporary(crntVal)); // NOSONAR

        // We translate the value into directions and use that to determine where to
        //   travel to
        if (matS.isMinima(crntVal)) {

            putConnectedComponentID(indxBuffer, indxGlobal);

            // We maintain a mapping between each minimas indxGlobal and
        } else if (matS.isConnectedComponentIDCode(crntVal)) {
            // NO CHANGE
        } else {
            int finalIndex = matS.calculateConnectedComponentID(point, crntVal);
            putCode(indxBuffer, finalIndex);
        }
    }

    private void putConnectedComponentID(int index, int connectedComponentID) {
        int encoded = encoding.encodeConnectedComponentID(connectedComponentID);
        delegate.putRaw(index, encoded);
    }
}
