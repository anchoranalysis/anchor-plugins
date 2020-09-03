package org.anchoranalysis.plugin.opencv.convert;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.function.Function;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.functional.RepeatUtilities;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBufferByte;
import org.anchoranalysis.image.voxel.buffer.VoxelBufferShort;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.image.voxel.factory.VoxelsFactoryTypeBound;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.bytedeco.javacv.OpenCVFrameConverter;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConvertFromMat {
    
    private static final OpenCVFrameConverter.ToMat CONVERTER = new OpenCVFrameConverter.ToMat();
    
    public static Stack toStack(Mat mat) throws OperationFailedException {
        
        if (mat.type() == CvType.CV_8UC1) {
            return toGrayscale(mat, VoxelBufferByte::wrap, VoxelsFactory.getByte());
        } else if (mat.type() == CvType.CV_16UC1) {
            return toGrayscale(mat, VoxelBufferShort::wrap, VoxelsFactory.getShort());
        } else if (mat.type() == CvType.CV_8UC3) {
            return toRGB(mat);
        } else {
            throw new OperationFailedException("Only unsigned 8-bit grayscale and RGB images are current supported for conversion");
        }
    }

    private static <T extends Buffer> Stack toGrayscale(Mat mat, Function<T,VoxelBuffer<T>> createVoxelBuffer, VoxelsFactoryTypeBound<T> factory) {

        Dimensions dimensions = dimensionsFrom(mat);
        
        org.bytedeco.opencv.opencv_core.Mat matConverted = CONVERTER.convertToMat( CONVERTER.convert(mat) );
        
        VoxelBuffer<T> buffer = createVoxelBuffer.apply( matConverted.createBuffer() );
        
        Voxels<T> voxels = factory.createForBuffer(buffer, dimensions.extent());
        
        Channel channel = ChannelFactory.instance().create(voxels);
        return new Stack(channel);
    }
    
    private static Stack toRGB(Mat mat) {
        Stack stack = createEmptyStack( dimensionsFrom(mat), 3 );
        matToRGB(mat, stack.getChannel(0), stack.getChannel(1), stack.getChannel(2));
        return stack;
    }
    
    private static void matToRGB(
            Mat mat, Channel channelRed, Channel channelGreen, Channel channelBlue) {

        Extent extent = channelRed.extent();
        Preconditions.checkArgument(extent.z() == 1);

        ByteBuffer red = BufferHelper.bufferFromChannel(channelRed);
        ByteBuffer green = BufferHelper.bufferFromChannel(channelGreen);
        ByteBuffer blue = BufferHelper.bufferFromChannel(channelBlue);

        byte[] arr = new byte[3];

        for (int y = 0; y < extent.y(); y++) {
            for (int x = 0; x < extent.x(); x++) {

                mat.get(y, x, arr);

                // OpenCV uses a BGR order as opposed to RGB in Anchor.
                blue.put(arr[0]);
                green.put(arr[1]);
                red.put(arr[2]);
            }
        }

        assert (!red.hasRemaining());
        assert (!green.hasRemaining());
        assert (!blue.hasRemaining());
    }
        
    private static Stack createEmptyStack(Dimensions dimensions, int numberChannels) {
        Stack stack = new Stack();
        RepeatUtilities.repeat(numberChannels, ()-> {
            try {
                stack.addChannel( ChannelFactory.instance().create(dimensions, UnsignedByteVoxelType.INSTANCE) );
            } catch (IncorrectImageSizeException e) {
                throw new AnchorImpossibleSituationException();
            }
        } );
        return stack;
    }
        
    private static Dimensions dimensionsFrom(Mat mat) {
        return new Dimensions(mat.size(1), mat.size(0), 1);
    }
}
