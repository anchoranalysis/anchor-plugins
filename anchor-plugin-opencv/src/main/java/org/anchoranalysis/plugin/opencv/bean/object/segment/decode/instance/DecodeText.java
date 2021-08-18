/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.plugin.image.segment.LabelledWithConfidence;
import org.anchoranalysis.plugin.opencv.segment.InferenceContext;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

/**
 * Extracts text from a RGB image by using the EAST deep neural network model
 *
 * <p>Each object-mask represented rotated-bounding box and is associated with a confidence score
 *
 * <p>Particular thanks to <a
 * href="https://www.pyimagesearch.com/2018/08/20/opencv-text-detection-east-text-detector/">Adrian
 * Rosebrock</a> whose tutorial was useful in applying this model
 *
 * @author Owen Feehan
 */
public class DecodeText extends DecodeInstanceSegmentation {

    private static final Scalar MEAN_SUBTRACTION_CONSTANTS = new Scalar(123.68, 116.78, 103.94);

    private static final String OUTPUT_SCORES = "feature_fusion/Conv_7/Sigmoid";
    private static final String OUTPUT_GEOMETRY = "feature_fusion/concat_3";

    // START BEAN PROPERTIES
    /** Proposed bounding boxes below this confidence interval are removed */
    @BeanField @Getter @Setter private double minConfidence = 0.5;
    // END BEAN PROPERTIES

    @Override
    public List<String> expectedOutputs() {
        return Arrays.asList(OUTPUT_SCORES, OUTPUT_GEOMETRY);
    }

    @Override
    public Scalar meanSubtractionConstants() {
        return MEAN_SUBTRACTION_CONSTANTS;
    }

    @Override
    public Stream<LabelledWithConfidence<ObjectMask>> decode(
            List<Mat> inferenceOutput, InferenceContext context) {
        List<LabelledWithConfidence<Mark>> marks =
                EastMarkExtracter.decode(inferenceOutput, minConfidence);
        ObjectScaledConverter converter =
                new ObjectScaledConverter(context.getScaleFactor(), context.getDimensions());
        return marks.stream().map(converter::convert);
    }
}
