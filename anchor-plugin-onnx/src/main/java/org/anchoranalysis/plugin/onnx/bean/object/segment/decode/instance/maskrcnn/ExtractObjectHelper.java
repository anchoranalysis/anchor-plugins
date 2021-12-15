package org.anchoranalysis.plugin.onnx.bean.object.segment.decode.instance.maskrcnn;

import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.inference.ImageInferenceContext;
import org.anchoranalysis.image.inference.segment.LabelledWithConfidence;
import org.anchoranalysis.image.inference.segment.MultiScaleObject;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.point.Point3d;
import org.anchoranalysis.spatial.point.PointConverter;

/**
 * Extracts a {@link MultiScaleObject} from a particular proposal in the respective buffers.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ExtractObjectHelper {

    /** A fallback object-class-label used, if no class-labels are provided to index. */
    private static final String OBJECT_CLASS_LABEL_FALLBACK = "unknown";

    /**
     * Extracts a {@link MultiScaleObject} from a particular proposal in the respective buffers.
     *
     * @param index the position of the proposal to extract (zero-indexed) .
     * @param scores scores for all proposals.
     * @param masks the encoded masks for all proposals.
     * @param labels the encoded labels for all proposals.
     * @param boxes the encoded boxes for all proposals.
     * @param minMaskValue only voxels with a value greater or equal to this threshold are
     *     considered as part of the mask.
     * @param context context-objects for performing image-inference.
     * @return a {@link MultiScaleObject} with an associated label and confidence, if at least one
     *     voxel is present in the mask.
     */
    public static Optional<LabelledWithConfidence<MultiScaleObject>> extractAt(
            int index,
            FloatBuffer scores,
            FloatBuffer masks,
            LongBuffer labels,
            FloatBuffer boxes,
            float minMaskValue,
            ImageInferenceContext context) {

        Optional<Voxels<FloatBuffer>> maskVoxels =
                ExtractMaskHelper.maskAtIndex(masks, index, minMaskValue);

        if (maskVoxels.isPresent()) {
            double score = scores.get(index);

            String label = deriveLabel(labels, index, context);
            BoundingBox box = boxAtIndex(boxes, index);

            MultiScaleObject objectAtScale =
                    MultiScaleObject.extractFrom(
                            context.scaleFactorUpscale(),
                            context.getDimensions(),
                            (factor, dimensions) ->
                                    ScaleMaskHelper.scaleMaskToFitBox(
                                            maskVoxels.get(),
                                            box,
                                            factor,
                                            context.getResizer(),
                                            dimensions,
                                            minMaskValue,
                                            context.getExecutionTimeRecorder()));
            return Optional.of(new LabelledWithConfidence<>(objectAtScale, score, label));
        } else {
            return Optional.empty();
        }
    }

    /** Extracts a {@link BoundingBox} at a particular position in the associated buffer. */
    private static BoundingBox boxAtIndex(FloatBuffer buffer, int boxIndex) {
        int indexMin = boxIndex * 4;
        Point3d min = new Point3d(buffer.get(indexMin), buffer.get(indexMin + 1), 0.0);
        Point3d max = new Point3d(buffer.get(indexMin + 2), buffer.get(indexMin + 3), 0.0);
        return new BoundingBox(
                PointConverter.intFromDouble(min, true), PointConverter.intFromDouble(max, true));
    }

    /**
     * Finds an associated label for an object, or else uses the fallback if not labels are defined.
     */
    private static String deriveLabel(LongBuffer labels, int index, ImageInferenceContext context) {
        return context.getClassLabels()
                .map(labelList -> labelList.get((int) labels.get(index)))
                .orElse(OBJECT_CLASS_LABEL_FALLBACK);
    }
}
