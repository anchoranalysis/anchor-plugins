/* (C)2020 */
package org.anchoranalysis.plugin.image.segment.watershed.encoding;

import org.anchoranalysis.core.geometry.Point3i;

// Encodes 3 things in an integer range
//   * A number of constants
//   * 27 Directions representing [-1,0,-1]X[-1,0,-1]X[-1,0,-1]
//   * A sequence of connected components IDs starting at 0
public class WatershedEncoding implements EncodeDirection {

    public static final int CODE_UNVISITED = 0;
    public static final int CODE_TEMPORARY = 1;
    public static final int CODE_MINIMA = 2;
    public static final int CODE_PLATEAU = 3;

    private static final int START_CHAIN_CODE_RANGE = CODE_PLATEAU + 1;
    private static final int END_CHAIN_CODE_RANGE =
            START_CHAIN_CODE_RANGE + ChainCodesDirection.MAX_VALUE;

    @Override
    public int encodeDirection(int x, int y, int z) {
        return ChainCodesDirection.chainCode(x, y, z) + START_CHAIN_CODE_RANGE;
    }

    /**
     * Decodes a chain-code into a point
     *
     * <p>TODO is it a good idea to cache the creation of chain codes, to avoid work on the heap?
     * There is a finite number.
     *
     * @param chainCode the chain-code
     * @return a new point (always newly created) for the given chain-code.
     */
    public Point3i chainCodes(int chainCode) {
        return ChainCodesDirection.decode(chainCode - START_CHAIN_CODE_RANGE);
    }

    public int encodeConnectedComponentID(int connectedComponentID) {
        return connectedComponentID + END_CHAIN_CODE_RANGE;
    }

    public boolean isDirectionChainCode(int code) {
        return code >= START_CHAIN_CODE_RANGE && code < END_CHAIN_CODE_RANGE;
    }

    public boolean isConnectedComponentIDCode(int code) {
        return code >= END_CHAIN_CODE_RANGE;
    }

    public int decodeConnectedComponentID(int code) {
        return code - END_CHAIN_CODE_RANGE;
    }
}
