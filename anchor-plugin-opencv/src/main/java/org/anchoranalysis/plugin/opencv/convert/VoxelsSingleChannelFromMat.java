package org.anchoranalysis.plugin.opencv.convert;

import java.nio.FloatBuffer;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.VoxelBufferWrap;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedShortBuffer;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.spatial.Extent;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class VoxelsSingleChannelFromMat {

    public static Voxels<?> createVoxelBuffer(  // NOSONAR
            Mat mat, Extent extent) throws OperationFailedException {
        
        if (mat.depth() == CvType.CV_8U) {
            return unsignedByteFromMat(mat, extent);
            
        } else if (mat.depth() == CvType.CV_16U) {
            return unsignedShortFromMat(mat, extent);
            
        } else if (mat.depth() == CvType.CV_32F) {
            return floatFromMat(mat, extent);
        } else {
            throw new OperationFailedException("Unsupported OpenCV type: " + mat.depth());
        }
    }
    
    private static Voxels<UnsignedByteBuffer> unsignedByteFromMat(Mat mat, Extent extent) {
        UnsignedByteBuffer buffer = UnsignedByteBuffer.allocate(extent.volumeXY());
        mat.get(0,0,buffer.array());
        return VoxelsFactory.getUnsignedByte().createForVoxelBuffer(
                VoxelBufferWrap.unsignedByteBuffer(buffer), extent);
    }
    
    private static Voxels<UnsignedShortBuffer> unsignedShortFromMat(Mat mat, Extent extent) {
        UnsignedShortBuffer buffer = UnsignedShortBuffer.allocate(extent.volumeXY());
        mat.get(0,0,buffer.array());
        return VoxelsFactory.getUnsignedShort().createForVoxelBuffer(
                VoxelBufferWrap.unsignedShortBuffer(buffer), extent);
    }
    
    private static Voxels<FloatBuffer> floatFromMat(Mat mat, Extent extent) {
        FloatBuffer buffer = FloatBuffer.allocate(extent.volumeXY());
        mat.get(0,0,buffer.array()); 
        return VoxelsFactory.getFloat().createForVoxelBuffer(
                VoxelBufferWrap.floatBuffer(buffer), extent);        
    }
}
