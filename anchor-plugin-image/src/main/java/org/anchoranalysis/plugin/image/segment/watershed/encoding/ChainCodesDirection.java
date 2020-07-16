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
/* (C)2020 */
package org.anchoranalysis.plugin.image.segment.watershed.encoding;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.geometry.Point3i;

/**
 * Maps directions to chain codes
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChainCodesDirection {

    // Corresponds to code 0
    public static final int MAX_VALUE = 27;

    /**
     * Decodes a chain-code into a point
     *
     * <p>TODO is it a good idea to cache the creation of chain codes, to avoid work on the heap?
     * There is a finite number.
     *
     * @param chainCode the chain-code
     * @return a new point (always newly created) for the given chain-code.
     */
    public static Point3i decode(int chainCode) {
        return new Point3i(
                ChainCodesDirection.xFromChainCode(chainCode),
                ChainCodesDirection.yFromChainCode(chainCode),
                ChainCodesDirection.zFromChainCode(chainCode));
    }

    // x, y, z  are -1, 0 or 1, for 3^3 combinations
    public static int chainCode(int x, int y, int z) {
        return ((z + 1) * 9) + ((y + 1) * 3) + (x + 1);
    }

    public static int xFromChainCode(int chainCode) {
        return (chainCode % 3) - 1;
    }

    public static int yFromChainCode(int chainCode) {
        return ((chainCode % 9) / 3) - 1;
    }

    public static int zFromChainCode(int chainCode) {
        return (chainCode / 9) - 1;
    }
}
