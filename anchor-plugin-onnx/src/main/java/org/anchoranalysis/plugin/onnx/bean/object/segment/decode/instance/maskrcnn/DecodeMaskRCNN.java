/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.onnx.bean.object.segment.decode.instance.maskrcnn;

import ai.onnxruntime.OnnxTensor;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.bean.interpolator.Interpolator;
import org.anchoranalysis.image.inference.ImageInferenceContext;
import org.anchoranalysis.image.inference.bean.segment.instance.DecodeInstanceSegmentation;
import org.anchoranalysis.image.inference.segment.LabelledWithConfidence;
import org.anchoranalysis.image.inference.segment.MultiScaleObject;

/**
 * Decodes the inference output from a Mask-RCNN implementation.
 *
 * <p>It is designed to work with accompanying {@code MaskRCNN-10.onnx} in resources, which expects
 * an image of size 1088x800 (width x height) and may throw an error if the input-size is different
 * than this.
 *
 * <p>The ONNX file was obtained from <a
 * href="https://github.com/onnx/models/tree/master/vision/object_detection_segmentation/mask-rcnn">this
 * GitHub source</a>, which also describes its inputs and outputs.
 *
 * <p>This <a href="https://github.com/microsoft/onnxruntime/issues/1670">issue</a> may also be
 * relevant, discussing the error message that occurs when the size above is not used as the input.
 *
 * @author Owen Feehan
 */
public class DecodeMaskRCNN extends DecodeInstanceSegmentation<OnnxTensor> {

    /** Name of outputted tensor for labels. */
    private static final String TENSOR_LABELS = "6570";

    /** Name of outputted tensor for scores. */
    private static final String TENSOR_SCORES = "6572";

    /** Name of outputted tensor for encoded bounding-boxes. */
    private static final String TENSOR_BOXES = "6568";

    /** Name outputted tensor for object-masks. */
    private static final String TENSOR_MASKS = "6887";

    // START BEAN PROPERTIES
    /**
     * Only proposals outputted from the model with a score greater or equal to this threshold are
     * considered.
     */
    @BeanField @Getter @Setter private float minConfidence = 0.5f;

    /**
     * Only voxels with a value greater or equal to this threshold are considered as part of the
     * mask.
     */
    @BeanField @Getter @Setter private float minMaskValue = 0.5f;

    /** The interpolator to use for scaling images. */
    @BeanField @Getter @Setter @DefaultInstance private Interpolator interpolator;
    // END BEAN PROPERTIES

    @Override
    public List<String> expectedOutputs() {
        return Arrays.asList(TENSOR_BOXES, TENSOR_LABELS, TENSOR_SCORES, TENSOR_MASKS);
    }

    @Override
    public List<LabelledWithConfidence<MultiScaleObject>> decode(
            List<OnnxTensor> inferenceOutput, ImageInferenceContext context)
            throws OperationFailedException {

        FloatBuffer scores = inferenceOutput.get(2).getFloatBuffer();

        List<Integer> indices = indicesAboveThreshold(scores);

        if (indices.isEmpty()) {
            return new ArrayList<>();
        }

        return extractObjects(indices, scores, inferenceOutput, context);
    }

    /**
     * Extract all {@link MultiScaleObject}s at particular indices.
     *
     * @throws OperationFailedException if the mask-buffer size is not as expected.
     */
    private List<LabelledWithConfidence<MultiScaleObject>> extractObjects(
            List<Integer> indices,
            FloatBuffer scores,
            List<OnnxTensor> inferenceOutput,
            ImageInferenceContext context)
            throws OperationFailedException {
        int numberProposals = scores.capacity();
        FloatBuffer masks = inferenceOutput.get(3).getFloatBuffer();
        ExtractMaskHelper.checkMaskBufferSize(masks, numberProposals);

        LongBuffer labels = inferenceOutput.get(1).getLongBuffer();

        FloatBuffer boxes = inferenceOutput.get(0).getFloatBuffer();

        return FunctionalList.mapToListOptional(
                indices,
                index ->
                        ExtractObjectHelper.extractAt(
                                index, scores, masks, labels, boxes, minMaskValue, context));
    }

    /**
     * Finds the indices of score entries that are above the threshold.
     *
     * @return a list of the indices of the elements above the threshold, in ascending index order.
     */
    private List<Integer> indicesAboveThreshold(FloatBuffer scores) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < scores.capacity(); i++) {
            if (scores.get(i) >= minConfidence) {
                indices.add(i);
            }
        }
        return indices;
    }
}
