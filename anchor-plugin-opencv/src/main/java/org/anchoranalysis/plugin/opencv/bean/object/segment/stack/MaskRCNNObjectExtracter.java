package org.anchoranalysis.plugin.opencv.bean.object.segment.stack;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBufferFactory;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.image.voxel.interpolator.Interpolator;
import org.anchoranalysis.image.voxel.interpolator.InterpolatorImgLib2;
import org.anchoranalysis.image.voxel.interpolator.InterpolatorImgLib2Lanczos;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.image.voxel.thresholder.VoxelsThresholder;
import org.anchoranalysis.plugin.image.segment.LabelledWithConfidence;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.point.Point3f;
import org.anchoranalysis.spatial.point.PointConverter;
import org.opencv.core.Mat;

/**
 * Extracts object-masks from the Mask RCNN model output.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MaskRCNNObjectExtracter {

    private static final Interpolator INTERPOLATOR = createInterpolator();

    /** Then number of bounding boxes that Mask RCNN returns. */
    private static final int NUMBER_BOXES_DETECTED = 100;

    /** The number of variables to describe an individual bounding-box. */
    private static final int BOX_CODING_SIZE = 7;

    /** Number of object-classes in Mask RCNN model. */
    private static final int NUMBER_OBJECT_CLASSES = 90;

    /** The width and length in pixels of the object-mask produced by the Mask RCNN model. */
    private static final int MASK_EXTENT = 15;

    /** The number of pixels in the area the object-mask produced by the Mask RCNN model. */
    private static final int MASK_AREA = MASK_EXTENT * MASK_EXTENT;

    /**
     * As a mask is returned for every object-class in the model output, this is the total number of
     * floats returned per box.
     */
    private static final int ALL_MASKS_ARRAY_SIZE = MASK_AREA * NUMBER_OBJECT_CLASSES;

    private static final int DETECTION_MATRIX_NUMBER_ELEMENTS =
            NUMBER_BOXES_DETECTED * BOX_CODING_SIZE;

    /**
     * Extracts object-masks from the tensors returned as model output from Mask R-CNN inference.
     *
     * @param boxes the tensor describing the bounding-boxes and object-class and confidence
     * @param masks the tensor describing the object-masks associated with the bounding-boxes
     * @param unscaledSize the same of the image before any scaling was applied to match model input
     * @param minConfidence a threshold below which we disconsider any proposed bounding-boxes
     * @param maskMinValue only intensity-values greater or equal to this threshold are considered
     *     to belong to the mask
     * @param objectClassLabels object-class labels, the corresponding index of the element
     *     describes the label
     * @return a list of newly created extracted objects (greater or equal to the confidence level)
     *     from the tensors.
     */
    public static List<LabelledWithConfidence<ObjectMask>> extractMasks(
            Mat boxes,
            Mat masks,
            Extent unscaledSize,
            float minConfidence,
            float maskMinValue,
            List<String> objectClassLabels) {

        // Reshape to be two dimensional arrays
        boxes = boxes.reshape(1, NUMBER_BOXES_DETECTED);
        masks = masks.reshape(1, NUMBER_BOXES_DETECTED);

        List<LabelledWithConfidence<ObjectMask>> out = new ArrayList<>();
        for (int i = 0; i < NUMBER_BOXES_DETECTED; i++) {

            float[] coded =
                    MatExtracter.extractFloatArray(boxes, i, DETECTION_MATRIX_NUMBER_ELEMENTS);

            Optional<LabelledWithConfidence<ObjectMask>> object =
                    extractFromCode(
                            coded,
                            masks,
                            i,
                            unscaledSize,
                            minConfidence,
                            maskMinValue,
                            objectClassLabels);
            object.ifPresent(out::add);
        }
        return out;
    }

    private static Optional<LabelledWithConfidence<ObjectMask>> extractFromCode(
            float[] coded,
            Mat masks,
            int index,
            Extent unscaledSize,
            float minConfidence,
            float maskMinValue,
            List<String> objectClassLabels) {
        BoundingBox box = extractBox(coded, unscaledSize);
        double confidence = coded[2];

        if (!Double.isNaN(confidence) && confidence >= minConfidence) {

            int objectClassIdentifier = (int) coded[1];

            BinaryVoxels<UnsignedByteBuffer> mask =
                    extractScaledMask(
                            masks, index, box.extent(), objectClassIdentifier, maskMinValue);

            ObjectMask object = new ObjectMask(box, mask);
            String label = objectClassLabels.get(objectClassIdentifier);
            return Optional.of(new LabelledWithConfidence<ObjectMask>(object, confidence, label));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Extracts a bounding-box from an encoded array.
     *
     * @param coded the encoded array describing a bounding-box.
     * @param unscaledSize the size of the image before any scaling (to input into the model)
     * @return a bounding box in the unscaled image as extracted from {@code coded}.
     */
    private static BoundingBox extractBox(float[] coded, Extent unscaledSize) {

        Point3f min = new Point3f(coded[3] * unscaledSize.x(), coded[4] * unscaledSize.y(), 0.0f);
        Point3f max = new Point3f(coded[5] * unscaledSize.x(), coded[6] * unscaledSize.y(), 0.0f);
        BoundingBox box =
                new BoundingBox(
                        PointConverter.intFromFloat(min, true),
                        PointConverter.intFromFloat(max, true));
        box = box.clampTo(unscaledSize);
        return box;
    }

    /** The voxels used to define the mask, scaled to match the bounding box. */
    private static BinaryVoxels<UnsignedByteBuffer> extractScaledMask(
            Mat masks, int row, Extent boxExtent, int objectClassIdentifier, float maskMinValue) {
        Voxels<FloatBuffer> maskVoxels = extractMaskVoxels(masks, row, objectClassIdentifier);

        // Scale and interpolate voxels to march the bounding-box
        maskVoxels = maskVoxels.extract().resizedXY(boxExtent.x(), boxExtent.y(), INTERPOLATOR);

        return VoxelsThresholder.thresholdForLevelFloat(
                maskVoxels, maskMinValue, BinaryValuesByte.getDefault());
    }

    /**
     * The voxels used to define the mask, at the same {@code MASK_AREA x MASK_AREA} original scale
     * from the mask output.
     */
    private static Voxels<FloatBuffer> extractMaskVoxels(
            Mat masks, int row, int objectClassIdentifier) {

        // All the masks for a given box (index by a row)
        float[] allMasks = MatExtracter.extractFloatArray(masks, row, ALL_MASKS_ARRAY_SIZE);

        VoxelBuffer<FloatBuffer> buffer = VoxelBufferFactory.allocateFloat(MASK_AREA);
        buffer.buffer().put(allMasks, objectClassIdentifier * MASK_AREA, MASK_AREA);
        return VoxelsFactory.getFloat()
                .createForVoxelBuffer(buffer, new Extent(MASK_EXTENT, MASK_EXTENT, 1));
    }

    /** The interpolator used for scaling the predicted mask up to the entire bounding-box. */
    private static Interpolator createInterpolator() {
        InterpolatorImgLib2 interpolator = new InterpolatorImgLib2Lanczos();
        // To avoid using the default mirroring strategy. Instead we wish to consider pixels outside
        // the box as being zero-valued.
        interpolator.extendWith(0);
        return interpolator;
    }
}