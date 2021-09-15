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
package org.anchoranalysis.plugin.opencv.bean.object.segment.stack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.concurrency.ConcurrentModel;
import org.anchoranalysis.core.concurrency.ConcurrentModelException;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.image.segment.LabelledWithConfidence;
import org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance.DecodeInstanceSegmentation;
import org.anchoranalysis.plugin.opencv.segment.InferenceContext;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

/** Helps perform inference on an image. */
@RequiredArgsConstructor
class InferenceHelper {

    /** Decodes inference output into segmented objects. */
    private final DecodeInstanceSegmentation decode;

    /** Subtract mean before channels. */
    private final double[] subtractMean;

    /**
     * Performs inference, and decodes the {@link Mat} into segmented-objects.
     *
     * @param image an OpenCV matrix containing the image to be segmented, already resized to the
     *     desired scale for inputting to the model for inference.
     * @param modelPool the models used for CNN inference
     * @param context the context of the inference
     * @return the results of the segmentation
     * @throws Throwable
     */
    public SegmentedObjects queueInference(
            Mat image, ConcurrentModelPool<Net> modelPool, InferenceContext context)
            throws Throwable {
        Stream<LabelledWithConfidence<ObjectMask>> objects =
                modelPool.executeOrWait(model -> performInference(model, image, context));
        return new SegmentedObjects(objects);
    }

    /** Performs inference on an {@code image} using {@code model}. */
    private Stream<LabelledWithConfidence<ObjectMask>> performInference(
            ConcurrentModel<Net> model, Mat image, InferenceContext context)
            throws ConcurrentModelException {
        try {
            InferenceExecutionTimeRecorder recorder =
                    new InferenceExecutionTimeRecorder(
                            context.getExecutionTimeRecorder(), model.isGpu());

            configureInput(model, image, recorder);

            return forwardPassAndDecode(model, recorder, context);

        } catch (Exception e) {
            throw new ConcurrentModelException(e);
        }
    }

    /** Associates the image with the mode. */
    private void configureInput(
            ConcurrentModel<Net> model, Mat image, InferenceExecutionTimeRecorder recorder) {
        try {
            Mat blob =
                    Dnn.blobFromImage(
                            image, 1.0, image.size(), new Scalar(subtractMean), false, false);
            model.getModel().setInput(blob);
        } finally {
            recorder.recordPre();
        }
    }

    /** Performs a forward-pass through the Neural Network model, and decodes the output. */
    private Stream<LabelledWithConfidence<ObjectMask>> forwardPassAndDecode(
            ConcurrentModel<Net> model,
            InferenceExecutionTimeRecorder recorder,
            InferenceContext context) {
        List<Mat> output = new ArrayList<>();
        try {
            model.getModel().forward(output, decode.expectedOutputs());
        } finally {
            recorder.recordInference();
        }

        try {
            return decode.decode(output, context);
        } finally {
            recorder.recordPost();
        }
    }
}
