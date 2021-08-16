package org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.concurrency.ConcurrentModelException;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.image.segment.LabelledWithConfidence;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import org.opencv.core.Mat;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

/**
 * Segments an image according a Mask-RCNN model with an Inception backbone trained on the COCO
 * dataset.
 *
 * <p>See the <a
 * href="https://learnopencv.com/deep-learning-based-object-detection-and-instance-segmentation-using-mask-rcnn-in-opencv-python-c/">tutorial</a>
 * that provided inspiration.
 *
 * @author Owen Feehan
 */
public class DecodeMaskRCNN extends DecodeInstanceSegmentation {

    /** Name of model output for encoded bounding-boxes. */
    private static final String OUTPUT_FINAL = "detection_out_final";

    /** Name of model output for object-masks. */
    private static final String OUTPUT_MASKS = "detection_masks";

    // START BEAN PROPERTIES
    /**
     * Only proposals outputted from the model with a score greater or equal to this threshold are
     * considered.
     */
    @BeanField @Getter @Setter private float minConfidence = 0.5f;

    /** Threshold above which pixels are considered in the mask. */
    @BeanField @Getter @Setter private float minMaskValue = 0.3f;
    // END BEAN PROPERTIES

    @Override
    public SegmentedObjects segmentMat(
            Mat mat,
            Optional<Resolution> resolution,
            Extent unscaledSize,
            ScaleFactor scaleFactor,
            ConcurrentModelPool<Net> modelPool, Optional<List<String>> classLabels)
            throws Throwable {
        
        if (!classLabels.isPresent()) {
            throw new OperationFailedException("Class-labels must be specified, but are not.");
        }

        return new SegmentedObjects(inferenceOnMat(modelPool, mat, unscaledSize, classLabels.get()).stream());
    }

    /** Performs inference to generate the masks. */
    private List<LabelledWithConfidence<ObjectMask>> inferenceOnMat(
            ConcurrentModelPool<Net> modelPool, Mat image, Extent unscaledSize, List<String> classLabels) throws Throwable {
        return modelPool.excuteOrWait(model -> forwardPass(model, image, unscaledSize, classLabels));
    }

    /** Performs the inference on a single image using the CNN model. */
    private List<LabelledWithConfidence<ObjectMask>> forwardPass(
            Net model, Mat image, Extent unscaledSize, List<String> classLabels) throws ConcurrentModelException {
        try {
            model.setInput(Dnn.blobFromImage(image));

            List<Mat> output = new ArrayList<>();
            model.forward(output, Arrays.asList(OUTPUT_FINAL, OUTPUT_MASKS));

            Mat boxes = output.get(0);
            Mat masks = output.get(1);

            return MaskRCNNObjectExtracter.extractMasks(
                    boxes, masks, unscaledSize, minConfidence, minMaskValue, classLabels);
        } catch (Exception e) {
            throw new ConcurrentModelException(e);
        }
    }
}
