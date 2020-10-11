package org.anchoranalysis.plugin.io.bean.stack.writer.bioformats;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.UnsignedIntBuffer;
import org.anchoranalysis.image.convert.UnsignedShortBuffer;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.datatype.FloatVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedIntVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class ByteRepresentationFactory {

    /**
     * Constructs a means of fetching representation of a slice of a channel in bytes for a particular target destination type.
     * 
     * <p>An assumption is that it will only be used for getting a byte-representation of the
     * same voxel-type or <i>upcasting</i> to a type of higher bit depth.
     * 
     * @param channel the channel from which a slice is extracted to form a byte-representation
     * @param destinationType the type we wish to save the channel in the image as (and with which we need a byte representation in this form).
     * @return a means of fetching a byte-representation for a slice of a channel
     */
    public static ByteRepresentationForChannel byteRepresentationFor(Channel channel, VoxelDataType destinationType) {
        VoxelDataType sourceDataType = channel.getVoxelDataType();
        return sliceIndex -> convertTo(channel.voxels().slice(sliceIndex), sourceDataType, destinationType);
    }
    
    private static byte[] convertTo( VoxelBuffer<?> buffer, VoxelDataType sourceDataType, VoxelDataType destinationType) throws RasterIOException {
        
        if (sourceDataType.equals(destinationType)) {
            return buffer.underlyingBytes();
        }
        
        if (destinationType.equals(UnsignedShortVoxelType.INSTANCE)) {
            return toUnsignedShort(buffer);
        } else if (destinationType.equals(UnsignedIntVoxelType.INSTANCE)) {
            return toUnsignedInt(buffer);
        } else if (destinationType.equals(FloatVoxelType.INSTANCE)) {
            return toFloat(buffer);            
        } else {
            throw new RasterIOException("Unsupported destination-type for representation in bytes: " + destinationType);
        }
    }
    
    private static byte[] toUnsignedShort(VoxelBuffer<?> in) {
        int index = 0;
        ByteBuffer byteBuffer = ByteBuffer.allocate(in.capacity() * 2);
        UnsignedShortBuffer shortBuffer = UnsignedShortBuffer.wrapRaw( byteBuffer.asShortBuffer() );
        while( shortBuffer.hasRemaining() ) {
            shortBuffer.putUnsigned( in.getInt(index++) );
        }
        return byteBuffer.array();
    }
    
    private static byte[] toUnsignedInt(VoxelBuffer<?> in) {
        int index = 0;
        ByteBuffer byteBuffer = ByteBuffer.allocate(in.capacity() * 4);
        UnsignedIntBuffer intBuffer = UnsignedIntBuffer.wrapRaw( byteBuffer.asIntBuffer() );
        while( intBuffer.hasRemaining() ) {
            intBuffer.putUnsigned( in.getInt(index++) );
        }
        return byteBuffer.array();
    }
    
    private static byte[] toFloat(VoxelBuffer<?> in) {
        int index = 0;
        ByteBuffer byteBuffer = ByteBuffer.allocate(in.capacity() * 4);
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        while( floatBuffer.hasRemaining() ) {
            floatBuffer.put( in.getInt(index++) );
        }
        return byteBuffer.array();
    }
}
