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

import java.util.List;
import java.util.stream.Stream;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.LabelledWithConfidence;
import org.anchoranalysis.plugin.opencv.segment.InferenceContext;
import org.opencv.core.Mat;

/**
 * Decodes inference output into segmented-objects.
 *
 * @author Owen Feehan
 */
public abstract class DecodeInstanceSegmentation extends AnchorBean<DecodeInstanceSegmentation> {

    /**
     * Decodes the output {@link Mat}s from inference into {@link ObjectMask}s with confidence and
     * labels.
     *
     * <p>The created {@link ObjectMask}s should match {@code unscaledDimensions} in size.
     *
     * @param inferenceOutput the {@link Mat}s that are the result of inference.
     * @param context the context in which the inference is occurring.
     * @return a newly created stream of objects, with associated confidence, and labels, that
     *     matches {@code unscaledDimensions} in size.
     */
    public abstract Stream<LabelledWithConfidence<ObjectMask>> decode(
            List<Mat> inferenceOutput, InferenceContext context);

    /**
     * Ordered names of the {@link Mat}s we are interested in processing, as outputted from
     * inference.
     */
    public abstract List<String> expectedOutputs();

    /**
     * A constant intensity for each respective channel to be subtracted before performing
     * inference.
     */
    public abstract double[] meanSubtractionConstants();
}
