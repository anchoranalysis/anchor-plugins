package org.anchoranalysis.plugin.opencv.bean.object.segment.stack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.concurrency.ConcurrentModelException;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.image.segment.WithConfidence;
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
public class SegmentMaskRCNN extends SegmentFromTensorFlowModel {

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
    protected String modelPath() {
        return "frozen_mask_rcnn_inception_v2_coco_2018_01_28.pb";
    }

    @Override
    protected Optional<String> textGraphPath() {
        return Optional.of("mask_rcnn_inception_v2_coco_2018_01_28.pbtxt");
    }

    @Override
    protected SegmentedObjects segmentMat(
            Mat mat,
            Optional<Resolution> resolution,
            Extent unscaledSize,
            ScaleFactor scaleFactor,
            ConcurrentModelPool<Net> modelPool)
            throws Throwable {
        return new SegmentedObjects(inferenceOnMat(modelPool, mat, unscaledSize));
    }

    @Override
    protected Extent inputSizeForModel(Extent imageSize) throws CreateException {
        return imageSize;
    }

    /** Performs inference to generate the masks. */
    private List<WithConfidence<ObjectMask>> inferenceOnMat(
            ConcurrentModelPool<Net> modelPool, Mat image, Extent unscaledSize) throws Throwable {
        return modelPool.excuteOrWait(model -> forwardPass(model, image, unscaledSize));
    }

    /** Performs the inference on a single image using the CNN model. */
    private List<WithConfidence<ObjectMask>> forwardPass(Net model, Mat image, Extent unscaledSize)
            throws ConcurrentModelException {
        try {
            model.setInput(Dnn.blobFromImage(image));

            List<Mat> output = new ArrayList<>();
            model.forward(output, Arrays.asList(OUTPUT_FINAL, OUTPUT_MASKS));

            Mat boxes = output.get(0);
            Mat masks = output.get(1);

            return MaskRCNNObjectExtracter.extractMasks(
                    boxes, masks, unscaledSize, minConfidence, minMaskValue);
        } catch (Exception e) {
            throw new ConcurrentModelException(e);
        }
    }
}
