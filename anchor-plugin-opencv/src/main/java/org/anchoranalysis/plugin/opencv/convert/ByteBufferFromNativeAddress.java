/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.opencv.convert;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Makes native directory memory accessible as a {@link ByteBuffer}.
 *
 * <p>From Peter Lawrey under <a href="https://creativecommons.org/licenses/by-sa/4.0/">CC BY-SA
 * 4.0</a> license.
 *
 * <p>See <a href="https://stackoverflow.com/a/52624632/14572996">Stack Overflow</a>.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ByteBufferFromNativeAddress {

    private static final Field address;

    private static final Field capacity;

    static {
        try {
            address = Buffer.class.getDeclaredField("address");
            address.setAccessible(true); // NOSONAR
            capacity = Buffer.class.getDeclaredField("capacity");
            capacity.setAccessible(true); // NOSONAR

        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Exposes a native address array of bytes as a {@link ByteBuffer}.
     *
     * @param nativeAddress the address in native memory where the bytes begin.
     * @param length how many bytes.
     * @return a new directly allocated {@link ByteBuffer} referencing the bytes at {@code
     *     nativeAddress}.
     */
    public static ByteBuffer wrapAddress(long nativeAddress, int length) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder());
        try {
            address.setLong(buffer, nativeAddress); // NOSONAR
            capacity.setInt(buffer, length); // NOSONAR
            buffer.clear();
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
        return buffer;
    }
}
