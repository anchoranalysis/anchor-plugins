package org.anchoranalysis.plugin.opencv.interpolator;

import java.nio.FloatBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedShortBuffer;
import org.anchoranalysis.image.voxel.interpolator.Interpolator;
import org.anchoranalysis.plugin.opencv.convert.ConvertToMat;
import org.anchoranalysis.plugin.opencv.convert.VoxelBufferFromMat;
import org.anchoranalysis.spatial.box.Extent;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Interpolates using OpenCV's {@code cv2.resize} function.
 *
 * @author Owen Feehan
 */
public class InterpolatorOpenCV extends Interpolator {

    @Override
    public boolean canValueRangeChange() {
        return true;
    }

    @Override
    protected VoxelBuffer<UnsignedByteBuffer> interpolateByte(
            VoxelBuffer<UnsignedByteBuffer> voxelsSource,
            VoxelBuffer<UnsignedByteBuffer> voxelsDestination,
            Extent extentSource,
            Extent extentDestination) {
        Mat unscaled = ConvertToMat.fromVoxelBufferByte(voxelsSource, extentSource);
        Mat scaled = resize(unscaled, extentDestination, CvType.CV_8UC1);
        return VoxelBufferFromMat.unsignedByteFromMat(scaled, extentDestination);
    }

    @Override
    protected VoxelBuffer<UnsignedShortBuffer> interpolateShort(
            VoxelBuffer<UnsignedShortBuffer> voxelsSource,
            VoxelBuffer<UnsignedShortBuffer> voxelsDestination,
            Extent extentSource,
            Extent extentDestination) {
        Mat unscaled = ConvertToMat.fromVoxelBufferShort(voxelsSource, extentSource);
        Mat scaled = resize(unscaled, extentDestination, CvType.CV_16UC1);
        return VoxelBufferFromMat.unsignedShortFromMat(scaled, extentDestination);
    }

    @Override
    protected VoxelBuffer<FloatBuffer> interpolateFloat(
            VoxelBuffer<FloatBuffer> voxelsSource,
            VoxelBuffer<FloatBuffer> voxelsDestination,
            Extent extentSource,
            Extent extentDestination) {
        Mat unscaled = ConvertToMat.fromVoxelBufferFloat(voxelsSource, extentSource);
        Mat scaled = resize(unscaled, extentDestination, CvType.CV_32FC1);
        return VoxelBufferFromMat.floatFromMat(scaled, extentDestination);
    }

    /** Performs the resize operation from one {@link Mat} to another. */
    private static Mat resize(Mat unscaled, Extent extentDestination, int type) {
        Size size = new Size(extentDestination.x(), extentDestination.y());
        Mat scaled = new Mat(size, type);
        // See
        // https://docs.opencv.org/3.1.0/da/d54/group__imgproc__transform.html#ga47a974309e9102f5f08231edc7e7529d
        Imgproc.resize(unscaled, scaled, size, 0.0, 0.0, Imgproc.INTER_AREA);
        return scaled;
    }
}
