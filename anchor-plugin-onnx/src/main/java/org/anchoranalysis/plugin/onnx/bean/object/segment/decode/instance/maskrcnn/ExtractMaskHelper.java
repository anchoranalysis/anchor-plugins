package org.anchoranalysis.plugin.onnx.bean.object.segment.decode.instance.maskrcnn;

import java.nio.FloatBuffer;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBufferWrap;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Extracts the voxels from a buffer that describe a mask.
 *
 * <p>The mask is always described by a constant number of voxels with constant width and height.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ExtractMaskHelper {

    /** The expected width and height of each produced mask. */
    private static final int WIDTH_HEIGHT_MASK = 28;

    /** The expected number of pixels in each produced mask. */
    private static final int NUMBER_PIXELS_MASK = WIDTH_HEIGHT_MASK * WIDTH_HEIGHT_MASK;

    /**
     * Throws a {@link OperationFailedException} if the buffer for the masks does not have the
     * expected size.
     *
     * @param buffer the buffer to check.
     * @param numberProposals the expected total number of proposed objects in the buffers.
     * @throws OperationFailedException if the number of elements in {@code buffer} is not as
     *     anticipated.
     */
    public static void checkMaskBufferSize(FloatBuffer buffer, int numberProposals)
            throws OperationFailedException {
        if (buffer.capacity() != (NUMBER_PIXELS_MASK * numberProposals)) {
            throw new OperationFailedException(
                    String.format(
                            "We expect %d number of pixels in each mask, but this isn't true.",
                            NUMBER_PIXELS_MASK));
        }
    }

    /**
     * Extracts the {@link Voxels} describing a mask at a particular index in the buffer.
     *
     * @param buffer the buffer containing voxels for many masks.
     * @param index the respective position in the buffer for the particular mask to extract
     *     (zero-indexed).
     * @param minMaskValue only voxels with a value greater or equal to this threshold are
     *     considered as part of the mask.
     * @return a newly created {@link Voxels}, if at least one voxel meets the threshold in {@code
     *     minMaskValue}.
     */
    public static Optional<Voxels<FloatBuffer>> maskAtIndex(
            FloatBuffer buffer, int index, float minMaskValue) {

        float[] extractedMaskPixels = new float[NUMBER_PIXELS_MASK];

        VoxelBuffer<FloatBuffer> voxelBuffer = VoxelBufferWrap.floatArray(extractedMaskPixels);

        buffer.position(index * NUMBER_PIXELS_MASK);
        buffer.get(extractedMaskPixels);

        if (!hasPixelAboveThreshold(extractedMaskPixels, minMaskValue)) {
            return Optional.empty();
        }

        Voxels<FloatBuffer> voxels =
                VoxelsFactory.getFloat()
                        .createForVoxelBuffer(
                                voxelBuffer, new Extent(WIDTH_HEIGHT_MASK, WIDTH_HEIGHT_MASK, 1));

        return Optional.of(voxels);
    }

    /** Is at least one pixel above the threshold? */
    private static boolean hasPixelAboveThreshold(float[] array, float minMaskValue) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] >= minMaskValue) {
                return true;
            }
        }
        return false;
    }
}
