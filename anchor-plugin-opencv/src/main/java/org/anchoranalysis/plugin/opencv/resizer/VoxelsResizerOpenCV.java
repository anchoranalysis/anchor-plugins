package org.anchoranalysis.plugin.opencv.resizer;

import java.nio.FloatBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedShortBuffer;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;
import org.anchoranalysis.plugin.opencv.convert.ConvertToMat;
import org.anchoranalysis.plugin.opencv.convert.VoxelBufferFromMat;
import org.anchoranalysis.spatial.box.Extent;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Resizes {@link VoxelBuffer}s using OpenCV's {@code cv2.resize} function.
 *
 * <p>It uses {@link Imgproc#INTER_AREA} interpolation when downsampling, and {@link
 * Imgproc#INTER_LINEAR} when upsampling.
 *
 * <p>See <a
 * href="https://docs.opencv.org/3.1.0/da/d54/group__imgproc__transform.html#ga47a974309e9102f5f08231edc7e7529d">OpenCV's
 * imresize documentation</a>.
 *
 * @author Owen Feehan
 */
public class VoxelsResizerOpenCV extends VoxelsResizer {

    @Override
    public boolean canValueRangeChange() {
        return true;
    }

    @Override
    protected VoxelBuffer<UnsignedByteBuffer> resizeByte(
            VoxelBuffer<UnsignedByteBuffer> voxelsSource,
            VoxelBuffer<UnsignedByteBuffer> voxelsDestination,
            Extent extentSource,
            Extent extentDestination) {
        Mat unscaled = ConvertToMat.fromVoxelBufferByte(voxelsSource, extentSource);
        Mat scaled = resize(unscaled, extentSource, extentDestination, CvType.CV_8UC1);
        return VoxelBufferFromMat.unsignedByteFromMat(scaled, extentDestination);
    }

    @Override
    protected VoxelBuffer<UnsignedShortBuffer> resizeShort(
            VoxelBuffer<UnsignedShortBuffer> voxelsSource,
            VoxelBuffer<UnsignedShortBuffer> voxelsDestination,
            Extent extentSource,
            Extent extentDestination) {
        Mat unscaled = ConvertToMat.fromVoxelBufferShort(voxelsSource, extentSource);
        Mat scaled = resize(unscaled, extentSource, extentDestination, CvType.CV_16UC1);
        return VoxelBufferFromMat.unsignedShortFromMat(scaled, extentDestination);
    }

    @Override
    protected VoxelBuffer<FloatBuffer> resizeFloat(
            VoxelBuffer<FloatBuffer> voxelsSource,
            VoxelBuffer<FloatBuffer> voxelsDestination,
            Extent extentSource,
            Extent extentDestination) {
        Mat unscaled = ConvertToMat.fromVoxelBufferFloat(voxelsSource, extentSource);
        Mat scaled = resize(unscaled, extentSource, extentDestination, CvType.CV_32FC1);
        return VoxelBufferFromMat.floatFromMat(scaled, extentDestination);
    }

    /** Performs the resize operation from one {@link Mat} to another. */
    private static Mat resize(
            Mat unscaled, Extent extentSource, Extent extentDestination, int type) {
        Size size = new Size(extentDestination.x(), extentDestination.y());
        Mat scaled = new Mat(size, type);
        // See
        // https://docs.opencv.org/3.1.0/da/d54/group__imgproc__transform.html#ga47a974309e9102f5f08231edc7e7529d
        Imgproc.resize(
                unscaled,
                scaled,
                size,
                0.0,
                0.0,
                selectInterpolator(extentSource, extentDestination));
        return scaled;
    }

    /**
     * Multiplexes between {@value ImgProc#INTER_AREA} (if downsampling) and {@value
     * ImgProc#INTER_LINEAR} if upsampling.
     */
    private static int selectInterpolator(Extent source, Extent destination) {
        if (destination.anyDimensionIsLargerThan(source)) {
            return Imgproc.INTER_LINEAR;
        } else {
            return Imgproc.INTER_AREA;
        }
    }
}
