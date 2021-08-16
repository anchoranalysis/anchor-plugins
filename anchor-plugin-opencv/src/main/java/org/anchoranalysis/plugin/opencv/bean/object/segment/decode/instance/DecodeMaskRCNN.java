package org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.image.segment.LabelledWithConfidence;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
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
            Dimensions dimensions,
            Extent unscaledSize,
            ScaleFactor scaleFactor,
            ConcurrentModelPool<Net> modelPool, Optional<List<String>> classLabels)
            throws Throwable {
        
        if (!classLabels.isPresent()) {
            throw new OperationFailedException("Class-labels must be specified, but are not.");
        }

        return new SegmentedObjects(queueInference(modelPool, mat, unscaledSize, classLabels, dimensions).stream());
    }
    
    @Override
    public List<String> expectedOutputs() {
        return Arrays.asList(OUTPUT_FINAL, OUTPUT_MASKS);
    }

    @Override
    public Scalar meanSubtractionConstants() {
        return new Scalar(0.0, 0.0, 0.0);
    }

    @Override
    protected List<LabelledWithConfidence<ObjectMask>> decode(
            List<Mat> output, Extent unscaledSize, Optional<List<String>> classLabels, Dimensions inputDimensions) {
        
        Mat boxes = output.get(0);
        Mat masks = output.get(1);

        return MaskRCNNObjectExtracter.extractMasks(
                boxes, masks, unscaledSize, minConfidence, minMaskValue, classLabels.get());    // NOSONAR
    }
}
