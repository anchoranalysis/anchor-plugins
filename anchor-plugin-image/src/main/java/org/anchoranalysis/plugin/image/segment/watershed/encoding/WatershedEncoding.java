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
