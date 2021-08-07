package org.anchoranalysis.plugin.opencv.bean.object.segment.stack;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.WithConfidence;
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
class ExtractMaskRCNNObjects {

    /** Then number of bounding boxes that Mask RCNN returns. */
    private static final int NUMBER_BOXES_DETECTED = 100;

    /** The number of variables to describe an individual bounding-box. */
    private static final int BOX_CODING_SIZE = 7;

    /** Number of object-classes in Mask RCNN model. */
    private static final int NUMBER_OBJECT_CLASSES = 90;

    /**
     * The number of pixels in the width and height of the object-mask produced by the Mask RCNN
     * model.
     */
    private static final int MASK_EXTENT = 255;

    private static final int DETECTION_MATRIX_NUMBER_ELEMENTS =
            NUMBER_BOXES_DETECTED * BOX_CODING_SIZE;

    public static List<WithConfidence<ObjectMask>> extractMasks(
            Mat boxes, Mat masks, Extent unscaledSize, double minConfidence) {

        // Reshape to be a two dimensional array
        boxes = boxes.reshape(1, NUMBER_BOXES_DETECTED);

        List<WithConfidence<ObjectMask>> out = new ArrayList<>();
        for (int i = 0; i < NUMBER_BOXES_DETECTED; i++) {

            float[] coded =
                    MatExtracter.extractFloatArray(boxes, i, DETECTION_MATRIX_NUMBER_ELEMENTS);

            BoundingBox box = extractBox(coded, unscaledSize);

            int objectClassIdentifier = (int) coded[1];
            double confidence = coded[2];

            if (!Double.isNaN(confidence) && confidence >= minConfidence) {
                ObjectMask object = new ObjectMask(box);
                object = object.invert();
                out.add(new WithConfidence<ObjectMask>(object, confidence));
            }
        }
        return out;
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
}
