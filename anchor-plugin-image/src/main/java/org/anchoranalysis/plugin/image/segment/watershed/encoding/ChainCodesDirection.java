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
