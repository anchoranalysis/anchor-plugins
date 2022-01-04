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

    private static final Field address, capacity;

    static {
        try {
            address = Buffer.class.getDeclaredField("address");
            address.setAccessible(true);
            capacity = Buffer.class.getDeclaredField("capacity");
            capacity.setAccessible(true);

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
            address.setLong(buffer, nativeAddress);
            capacity.setInt(buffer, length);
            buffer.clear();
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
        return buffer;
    }
}
