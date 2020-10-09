package org.anchoranalysis.plugin.io.bean.rasterwriter.bioformats;

import org.anchoranalysis.core.functional.function.CheckedRunnable;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.voxel.datatype.FloatVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedIntVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ome.xml.model.enums.PixelType;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class VoxelTypeHelper {
    
    public static PixelType pixelTypeFor(VoxelDataType dataType) throws RasterIOException {
        if (dataType.equals(UnsignedByteVoxelType.INSTANCE)) {
            return PixelType.UINT8;
        } else if (dataType.equals(UnsignedShortVoxelType.INSTANCE)) {
            return PixelType.UINT16;
        } else if (dataType.equals(UnsignedIntVoxelType.INSTANCE)) {
            return PixelType.UINT32;
        } else if (dataType.equals(FloatVoxelType.INSTANCE)) {
            return PixelType.FLOAT;
        } else {
            throw new RasterIOException(
                    String.format("%s is an unsupported data-type for this writer", dataType));
        }
    }
    
    public static void checkChannelTypeSupported(String messagePrefix, VoxelDataType channelType, CheckedRunnable<RasterIOException> runnable) throws RasterIOException {
        if (isChannelTypeSupported(channelType)) {
            runnable.run();
        } else {
            throw new RasterIOException(messagePrefix + "an unsupported type: " + messagePrefix);
        }  
    }
    
    private static boolean isChannelTypeSupported(VoxelDataType channelType) {
        return channelType.equals(UnsignedByteVoxelType.INSTANCE)
                || channelType.equals(UnsignedShortVoxelType.INSTANCE)
                || channelType.equals(UnsignedIntVoxelType.INSTANCE)
                || channelType.equals(FloatVoxelType.INSTANCE);
    }
}
