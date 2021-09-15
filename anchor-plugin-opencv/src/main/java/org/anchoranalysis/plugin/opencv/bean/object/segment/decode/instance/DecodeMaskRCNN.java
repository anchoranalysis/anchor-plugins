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
package org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.LabelledWithConfidence;
import org.anchoranalysis.plugin.opencv.segment.InferenceContext;
import org.opencv.core.Mat;

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
    public List<String> expectedOutputs() {
        return Arrays.asList(OUTPUT_FINAL, OUTPUT_MASKS);
    }

    @Override
    public Stream<LabelledWithConfidence<ObjectMask>> decode(
            List<Mat> inferenceOutput, InferenceContext context) {

        Mat boxes = inferenceOutput.get(0);
        Mat masks = inferenceOutput.get(1);

        return MaskRCNNObjectExtracter.extractMasks(
                boxes,
                masks,
                context.getDimensions().extent(),
                minConfidence,
                minMaskValue,
                context.getClassLabels())
                .stream();
    }
}
