package org.anchoranalysis.plugin.onnx.bean.object.segment.decode.instance;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.TensorInfo;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.inference.ImageInferenceContext;
import org.anchoranalysis.image.inference.bean.segment.instance.DecodeInstanceSegmentation;
import org.anchoranalysis.image.inference.segment.LabelledWithConfidence;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkToObjectConverter;
import org.anchoranalysis.mpp.mark.points.RotatableBoundingBoxFactory;
import org.anchoranalysis.spatial.point.Point2i;
import org.anchoranalysis.spatial.scale.ScaleFactorInt;

/**
 * Extracts text from a RGB image by using the <i>EAST deep neural network model</i> and the ONNX
 * Runtime.
 *
 * <p>Each object-mask represented rotated-bounding box and is associated with a confidence score.
 *
 * <p>Particular thanks to <a
 * href="https://www.pyimagesearch.com/2018/08/20/opencv-text-detection-east-text-detector/">Adrian
 * Rosebrock</a> whose tutorial was useful in applying this model
 *
 * @author Owen Feehan
 */
public class DecodeText extends DecodeInstanceSegmentation<OnnxTensor> {

    private static final String OUTPUT_SCORES = "feature_fusion/Conv_7/Sigmoid:0";
    private static final String OUTPUT_GEOMETRY = "feature_fusion/concat_3:0";

    private static final String CLASS_LABEL = "text";

    private static final ScaleFactorInt SCALE_BY_4 = new ScaleFactorInt(4, 4);

    /** Number of elements in each vector to describe a bounding-box. */
    private static final int VECTOR_SIZE = 5;

    // START BEAN PROPERTIES
    /** Proposed bounding boxes below this confidence interval are removed from consideration. */
    @BeanField @Getter @Setter private double minConfidence = 0.5;
    // END BEAN PROPERTIES

    @Override
    public List<LabelledWithConfidence<ObjectMask>> decode(
            List<OnnxTensor> inferenceOutput, ImageInferenceContext context)
            throws OperationFailedException {

        FloatBuffer scores = inferenceOutput.get(0).getFloatBuffer();

        List<Integer> indices = indicesAboveThreshold(scores);

        MarkToObjectConverter converter =
                new MarkToObjectConverter(context.getScaleFactor(), context.getDimensions());

        return extractObjects(inferenceOutput.get(1), scores, indices, converter);
    }

    @Override
    public List<String> expectedOutputs() {
        return Arrays.asList(OUTPUT_SCORES, OUTPUT_GEOMETRY);
    }

    /**
     * Find the indices of all proposals whose score is greater or equal to a confidence threshold.
     */
    private List<Integer> indicesAboveThreshold(FloatBuffer scores) {
        scores.rewind();

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < scores.capacity(); i++) {
            if (scores.get() >= minConfidence) {
                indices.add(i);
            }
        }
        return indices;
    }

    /**
     * Extract the bounding-boxes located at particular indices in the form of an {@link ObjectMask}
     * with an associated label and confidence.
     */
    private List<LabelledWithConfidence<ObjectMask>> extractObjects(
            OnnxTensor geometryTensor,
            FloatBuffer scores,
            List<Integer> indices,
            MarkToObjectConverter converter) {

        List<LabelledWithConfidence<ObjectMask>> out = new ArrayList<>();

        TensorInfo geometryInfo = geometryTensor.getInfo();
        int width = (int) geometryInfo.getShape()[1];
        int height = (int) geometryInfo.getShape()[2];

        FloatBuffer geometryBuffer = geometryTensor.getFloatBuffer();

        for (int index : indices) {
            int x = index % width;
            int y = index / height;
            Point2i anchorPointUnscaled = new Point2i(x, y);
            Point2i anchorPointScaled = SCALE_BY_4.scale(anchorPointUnscaled);
            out.add(
                    extractLabelledBoundingBox(
                            scores, geometryBuffer, index, anchorPointScaled, converter));
        }

        return out;
    }

    /** Extract a bounding-box together with a confidence and label at a particular index. */
    private LabelledWithConfidence<ObjectMask> extractLabelledBoundingBox(
            FloatBuffer scores,
            FloatBuffer geometry,
            int index,
            Point2i offset,
            MarkToObjectConverter converter) {
        int indexStart = index * VECTOR_SIZE;
        Mark mark =
                RotatableBoundingBoxFactory.create(
                        vectorIndex -> geometry.get(indexStart + vectorIndex), offset);

        ObjectMask objectMask = converter.convert(mark);
        return new LabelledWithConfidence<>(objectMask, scores.get(index), CLASS_LABEL);
    }
}
