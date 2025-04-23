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

import org.anchoranalysis.spatial.point.Point3i;

/**
 * Encodes watershed-related information in an integer range.
 *
 * <p>This class encodes three types of information:
 *
 * <ul>
 *   <li>A number of state-machine-type constants
 *   <li>27 Directions representing [-1,0,-1]X[-1,0,-1]X[-1,0,-1]
 *   <li>A sequence of connected components IDs starting at 0
 * </ul>
 */
public class WatershedEncoding implements EncodeDirection {

    /** Code representing an unvisited voxel. */
    public static final int CODE_UNVISITED = 0;

    /** Code representing a temporary voxel. */
    public static final int CODE_TEMPORARY = 1;

    /** Code representing a minima voxel. */
    public static final int CODE_MINIMA = 2;

    /** Code representing a plateau voxel. */
    public static final int CODE_PLATEAU = 3;

    /** The start of the chain code range. */
    private static final int START_CHAIN_CODE_RANGE = CODE_PLATEAU + 1;

    /** The end of the chain code range. */
    private static final int END_CHAIN_CODE_RANGE =
            START_CHAIN_CODE_RANGE + ChainCodesDirection.MAX_VALUE;

    /** {@inheritDoc} */
    @Override
    public int encodeDirection(int x, int y, int z) {
        return ChainCodesDirection.chainCode(x, y, z) + START_CHAIN_CODE_RANGE;
    }

    /**
     * Decodes a chain-code into a point.
     *
     * @param chainCode the chain-code to decode
     * @return a new {@link Point3i} (always newly created) for the given chain-code
     */
    public Point3i chainCodes(int chainCode) {
        return ChainCodesDirection.decode(chainCode - START_CHAIN_CODE_RANGE);
    }

    /**
     * Encodes a connected component ID.
     *
     * @param connectedComponentID the ID of the connected component to encode
     * @return the encoded value for the connected component ID
     */
    public int encodeConnectedComponentID(int connectedComponentID) {
        return connectedComponentID + END_CHAIN_CODE_RANGE;
    }

    /**
     * Checks if the given code represents a direction chain code.
     *
     * @param code the code to check
     * @return true if the code represents a direction chain code, false otherwise
     */
    public boolean isDirectionChainCode(int code) {
        return code >= START_CHAIN_CODE_RANGE && code < END_CHAIN_CODE_RANGE;
    }

    /**
     * Checks if the given code represents a connected component ID.
     *
     * @param code the code to check
     * @return true if the code represents a connected component ID, false otherwise
     */
    public boolean isConnectedComponentIDCode(int code) {
        return code >= END_CHAIN_CODE_RANGE;
    }

    /**
     * Decodes a connected component ID from its encoded form.
     *
     * @param code the encoded connected component ID
     * @return the decoded connected component ID
     */
    public int decodeConnectedComponentID(int code) {
        return code - END_CHAIN_CODE_RANGE;
    }
}
