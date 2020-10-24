/*-
 * #%L
 * anchor-plugin-opencv
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
package org.anchoranalysis.plugin.opencv.convert;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;

/**
 * Converts a <i>direct</i> NIO buffer to an array-backed buffer.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ConvertDirectBuffer {

    /**
     * Converts a <i>direct</i> NIO primitive buffer to an an array-backed buffer, if it is not
     * already.
     *
     * <p>The following types are supported:
     *
     * <ul>
     *   <li>{@link ByteBuffer}
     *   <li>{@link ShortBuffer}
     *   <li>{@link IntBuffer}
     *   <li>{@link FloatBuffer}
     * </ul>
     *
     * <p>The type of buffer is checked via reflection.
     *
     * @param <T> type of buffer
     * @param buffer the buffer to maybe convert
     * @return the existing buffer if it is array-backed, otherwise a newly created array-backed
     *     buffer with the contents copied into it.
     * @throws OperationFailedException if an unsupported buffer type is passed.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Buffer> T convertIfNeeded(T buffer) throws OperationFailedException {
        if (buffer.hasArray()) {
            return buffer;
        } else if (buffer instanceof ByteBuffer) {
            return (T) convertByte((ByteBuffer) buffer);
        } else if (buffer instanceof ShortBuffer) {
            return (T) convertShort((ShortBuffer) buffer);
        } else if (buffer instanceof IntBuffer) {
            return (T) convertInt((IntBuffer) buffer);
        } else if (buffer instanceof FloatBuffer) {
            return (T) convertFloat((FloatBuffer) buffer);
        } else {
            throw new OperationFailedException(
                    "Unsupported buffer-type for conversion: " + buffer.getClass());
        }
    }

    private static ByteBuffer convertByte(ByteBuffer buffer) {
        byte[] array = new byte[buffer.capacity()];
        buffer.put(array);
        return ByteBuffer.wrap(array);
    }

    private static ShortBuffer convertShort(ShortBuffer buffer) {
        short[] array = new short[buffer.capacity()];
        buffer.put(array);
        return ShortBuffer.wrap(array);
    }

    private static IntBuffer convertInt(IntBuffer buffer) {
        int[] array = new int[buffer.capacity()];
        buffer.put(array);
        return IntBuffer.wrap(array);
    }

    private static FloatBuffer convertFloat(FloatBuffer buffer) {
        float[] array = new float[buffer.capacity()];
        buffer.put(array);
        return FloatBuffer.wrap(array);
    }
}
