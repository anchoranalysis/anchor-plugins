package org.anchoranalysis.plugin.opencv.convert;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.anchoranalysis.core.exception.OperationFailedException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Converts a <i>direct</i> NIO buffer to an array-backed buffer.
 * 
 * @author Owen Feehan
 *
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
class ConvertDirectBuffer {
    
    /**
     * Converts a <i>direct</i> NIO primitive buffer to an an array-backed buffer, if it is not already.
     * 
     * <p>The following types are supported:
     * <ul>
     * <li>{@link ByteBuffer}
     * <li>{@link ShortBuffer}
     * <li>{@link IntBuffer}
     * <li>{@link FloatBuffer}
     * </ul>
     * 
     * <p>The type of buffer is checked via reflection.
     *
     * @param <T> type of buffer
     * @param buffer the buffer to maybe convert
     * @return the existing buffer if it is array-backed, otherwise a newly created array-backed buffer with the contents copied into it.
     * @throws OperationFailedException if an unsupported buffer type is passed.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Buffer> T convertIfNeeded(T buffer) throws OperationFailedException {
        if (buffer.hasArray()) {
            return buffer;
        } else if (buffer instanceof ByteBuffer) {
            return (T) convertByte( (ByteBuffer) buffer);
        } else if (buffer instanceof ShortBuffer) {
            return (T) convertShort( (ShortBuffer) buffer);
        } else if (buffer instanceof IntBuffer) {
            return (T) convertInt( (IntBuffer) buffer);
        } else if (buffer instanceof FloatBuffer) {
            return (T) convertFloat( (FloatBuffer) buffer);            
        } else {
            throw new OperationFailedException("Unsupported buffer-type for conversion: " + buffer.getClass());
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
