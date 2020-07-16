/* (C)2020 */
package org.anchoranalysis.plugin.image.segment.watershed.encoding;

import java.nio.IntBuffer;
import org.anchoranalysis.core.geometry.Point3i;

public final class EncodedIntBuffer {

    private final IntBuffer delegate;
    private final WatershedEncoding encoding;

    public EncodedIntBuffer(IntBuffer buffer, final WatershedEncoding encoding) {
        super();
        this.delegate = buffer;
        this.encoding = encoding;
    }

    public boolean isTemporary(int offset) {
        return (delegate.get(offset) == WatershedEncoding.CODE_TEMPORARY);
    }

    public boolean isUnvisited(int offset) {
        return (delegate.get(offset) == WatershedEncoding.CODE_UNVISITED);
    }

    public boolean isMinima(int offset) {
        return (delegate.get(offset) == WatershedEncoding.CODE_MINIMA);
    }

    public boolean isConnectedComponentID(int offset) {
        return (encoding.isConnectedComponentIDCode(delegate.get(offset)));
    }

    public void markAsTemporary(int offset) {
        delegate.put(offset, WatershedEncoding.CODE_TEMPORARY);
    }

    public int getCode(int index) {
        return delegate.get(index);
    }

    public IntBuffer putCode(int index, int code) {
        return delegate.put(index, code);
    }

    /** Convert code to connected-component */
    public void convertCode(int indxBuffer, int indxGlobal, EncodedVoxelBox matS, Point3i point) {
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

    private IntBuffer putConnectedComponentID(int index, int connectedComponentID) {
        int encoded = encoding.encodeConnectedComponentID(connectedComponentID);
        return delegate.put(index, encoded);
    }
}
