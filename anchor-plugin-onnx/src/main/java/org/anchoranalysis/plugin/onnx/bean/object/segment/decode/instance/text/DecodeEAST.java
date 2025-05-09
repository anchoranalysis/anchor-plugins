/*-
 * #%L
 * anchor-plugin-onnx
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package org.anchoranalysis.plugin.onnx.bean.object.segment.decode.instance.text;

import ai.onnxruntime.OnnxTensor;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.inference.ImageInferenceContext;
import org.anchoranalysis.image.inference.bean.segment.instance.DecodeInstanceSegmentation;
import org.anchoranalysis.image.inference.segment.DualScale;
import org.anchoranalysis.image.inference.segment.LabelledWithConfidence;
import org.anchoranalysis.image.inference.segment.MultiScaleObject;
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
public class DecodeEAST extends DecodeInstanceSegmentation<OnnxTensor> {

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
    public List<LabelledWithConfidence<MultiScaleObject>> decode(
            List<OnnxTensor> inferenceOutput, ImageInferenceContext context)
            throws OperationFailedException {

        FloatBuffer scores = inferenceOutput.get(0).getFloatBuffer();

        List<Integer> indices = indicesAboveThreshold(scores);

        return extractObjects(
                inferenceOutput.get(1),
                scores,
                indices,
                dualScaleConverters(context),
                context.getExecutionTimeRecorder());
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

        List<Integer> indices = new ArrayList<>(scores.capacity());
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
    private static List<LabelledWithConfidence<MultiScaleObject>> extractObjects(
            OnnxTensor geometryTensor,
            FloatBuffer scores,
            List<Integer> indices,
            DualScale<MarkToObjectConverter> converter,
            ExecutionTimeRecorder executionTimeRecorder) {

        List<LabelledWithConfidence<MultiScaleObject>> out = new ArrayList<>(indices.size());

        int height = (int) geometryTensor.getInfo().getShape()[2];

        FloatBuffer geometryBuffer = geometryTensor.getFloatBuffer();

        for (int index : indices) {
            int x = index % height;
            int y = index / height;
            Point2i anchorPointScaled = SCALE_BY_4.scale(x, y);
            out.add(
                    extractLabelledBoundingBox(
                            scores,
                            geometryBuffer,
                            index,
                            anchorPointScaled,
                            converter,
                            executionTimeRecorder));
        }

        return out;
    }

    /** Extract a bounding-box together with a confidence and label at a particular index. */
    private static LabelledWithConfidence<MultiScaleObject> extractLabelledBoundingBox(
            FloatBuffer scores,
            FloatBuffer geometry,
            int index,
            Point2i offset,
            DualScale<MarkToObjectConverter> convertersDual,
            ExecutionTimeRecorder executionTimeRecorder) {

        MultiScaleObject objectAtScale =
                MultiScaleObject.extractFrom(
                        convertersDual,
                        converter ->
                                createObjectFromGeometry(
                                        index, offset, geometry, converter, executionTimeRecorder));
        return new LabelledWithConfidence<>(objectAtScale, scores.get(index), CLASS_LABEL);
    }

    /**
     * Derive an {@link ObjectMask} from the rotated-bounding box entity described in the
     * <i>geometry</i> buffer at a particular index.
     */
    private static ObjectMask createObjectFromGeometry(
            int index,
            Point2i offset,
            FloatBuffer geometry,
            MarkToObjectConverter converter,
            ExecutionTimeRecorder executionTimeRecorder) {
        int indexStart = index * VECTOR_SIZE;
        Mark mark =
                RotatableBoundingBoxFactory.create(
                        vectorIndex -> geometry.get(indexStart + vectorIndex), offset);

        return executionTimeRecorder.recordExecutionTime(
                "Convert mark", () -> converter.convert(mark));
    }

    /** A {@link MarkToObjectConverter} for each respective scale. */
    private static DualScale<MarkToObjectConverter> dualScaleConverters(
            ImageInferenceContext context) {
        return context.scaleFactorUpscale()
                .combine(context.getDimensions(), MarkToObjectConverter::new);
    }
}
