package org.anchoranalysis.plugin.onnx.bean.object.segment.decode.instance.maskrcnn;

import java.nio.FloatBuffer;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.inference.segment.ScaleAndThresholdVoxels;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Scales the voxels that describe the mask to match the size of a {@link BoundingBox}.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ScaleMaskHelper {

    /** Scaler for the mask voxels. */
    private static final ScaleAndThresholdVoxels SCALER = new ScaleAndThresholdVoxels(false);

    /**
     * Scales the voxels that describe the mask to match the size of a {@link BoundingBox}.
     *
     * @param maskVoxels the voxels to scale.
     * @param box the box, optionally-scaled, who the the voxels are scaled to match.
     * @param scaleFactor if defined, the final box for the voxels is {@code box} multipled by this
     *     scaling factor.
     * @param clampTo a size in which the scaled voxels are forced to reside.
     * @param minMaskValue only voxels with a value greater or equal to this threshold are
     *     considered as part of the mask.
     * @param executionTimeRecorder records the time of particular operations.
     * @return a newly created {@link ObjectMask} derived from a scaled version of {@code
     *     maskVoxels} with bounding-box as described above.
     */
    public static ObjectMask scaleMaskToFitBox(
            Voxels<FloatBuffer> maskVoxels,
            BoundingBox box,
            Optional<ScaleFactor> scaleFactor,
            Dimensions clampTo,
            float minMaskValue,
            ExecutionTimeRecorder executionTimeRecorder) {
        box = clampMaybeScaleBox(box, scaleFactor, clampTo.extent());
        return scaleMaskToBox(maskVoxels, box, minMaskValue, executionTimeRecorder);
    }

    /** Clamps and maybe applies a scaling-factor to a {@link BoundingBox}. */
    private static BoundingBox clampMaybeScaleBox(
            BoundingBox box, Optional<ScaleFactor> scaleFactor, Extent extent) {
        if (scaleFactor.isPresent()) {
            return box.scale(scaleFactor.get()).clampTo(extent);
        } else {
            return box.clampTo(extent);
        }
    }

    /**
     * Scales the {@link Voxels} reflecting a mask, to be the same size as a {@link BoundingBox}.
     */
    private static ObjectMask scaleMaskToBox(
            Voxels<FloatBuffer> voxels,
            BoundingBox box,
            float minMaskValue,
            ExecutionTimeRecorder executionTimeRecorder) {
        BinaryVoxels<UnsignedByteBuffer> scaledMask =
                executionTimeRecorder.recordExecutionTime(
                        "Scale and threshold mask",
                        () -> SCALER.scaleAndThreshold(voxels, box.extent(), minMaskValue));

        return new ObjectMask(box, scaledMask);
    }
}
