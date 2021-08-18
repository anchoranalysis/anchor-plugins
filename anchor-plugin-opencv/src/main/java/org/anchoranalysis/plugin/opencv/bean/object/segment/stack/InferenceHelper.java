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
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.core.concurrency.ConcurrentModel;
import org.anchoranalysis.core.concurrency.ConcurrentModelException;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.image.segment.LabelledWithConfidence;
import org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance.DecodeInstanceSegmentation;
import org.anchoranalysis.plugin.opencv.segment.InferenceContext;
import org.opencv.core.Mat;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

/** Helps perform inference on an image. */
@RequiredArgsConstructor
class InferenceHelper {

    private static final String EXECUTION_TIME_MODEL_PRE_PROCESSING = "Pre-processing Inference";

    /**
     * Unique identifier for recording first-time operations on model inference, if GPU is enabled.
     */
    private static final String EXECUTION_TIME_MODEL_INFERENCE_WARM_UP =
            "Model Inference (GPU with Warm Up)";

    /** Unique identifier for recording subsequent operations on model inference. */
    private static final String EXECUTION_TIME_MODEL_INFERENCE_SUBSEQUENT = "Model Inference";

    private static final String EXECUTION_TIME_MODEL_POST_PROCESSING = "Post-processing Inference";

    /** Decodes inference output into segmented objects. */
    private final DecodeInstanceSegmentation decode;

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
                modelPool.excuteOrWait(model -> performInference(model, image, context));
        return new SegmentedObjects(objects);
    }

    private Stream<LabelledWithConfidence<ObjectMask>> performInference(
            ConcurrentModel<Net> model, Mat image, InferenceContext context)
            throws ConcurrentModelException {
        try {
            long timestamp = System.currentTimeMillis();

            try {
                model.getModel()
                        .setInput(
                                Dnn.blobFromImage(
                                        image,
                                        1.0,
                                        image.size(),
                                        decode.meanSubtractionConstants(),
                                        false,
                                        false));
            } finally {
                timestamp =
                        recordExecutionTime(
                                addSuffix(EXECUTION_TIME_MODEL_PRE_PROCESSING, model.isGpu()),
                                Optional.empty(),
                                timestamp,
                                context);
            }

            List<Mat> output = new ArrayList<>();
            try {
                model.getModel().forward(output, decode.expectedOutputs());
            } finally {
                timestamp =
                        recordExecutionTime(
                                addSuffix(EXECUTION_TIME_MODEL_INFERENCE_SUBSEQUENT, model.isGpu()),
                                OptionalFactory.create(
                                        model.isGpu(),
                                        () -> EXECUTION_TIME_MODEL_INFERENCE_WARM_UP),
                                timestamp,
                                context);
            }

            try {
                return decode.decode(output, context);
            } finally {
                recordExecutionTime(
                        EXECUTION_TIME_MODEL_POST_PROCESSING, Optional.empty(), timestamp, context);
            }

        } catch (Exception e) {
            throw new ConcurrentModelException(e);
        }
    }

    /**
     * Records the execution-time of how long a particular operation took.
     *
     * <p>The operation is presumed to end when this function is called.
     *
     * @param operationIdentifier the unique name of the operation to record the time against
     * @param alternativeIdentifierIfFirst an alternative unique to use if this is the first time
     *     the execution-time is recorded.
     * @param previousTimestamp the timestamp describing millis from the epoch at the start of the
     *     operation
     * @param context the context containing a logger
     * @return the current timestamp (millis from the epoch) used to measure the end of the
     *     operation
     */
    private static long recordExecutionTime(
            String operationIdentifier,
            Optional<String> alternativeIdentifierIfFirst,
            long previousTimestamp,
            InferenceContext context) {
        long currentTimestamp = System.currentTimeMillis();
        long executionTime = currentTimestamp - previousTimestamp;
        if (alternativeIdentifierIfFirst.isPresent()) {
            context.getExecutionTimeRecorder()
                    .recordExecutionTime(
                            alternativeIdentifierIfFirst.get(), operationIdentifier, executionTime);
        } else {
            context.getExecutionTimeRecorder()
                    .recordTimeDifferenceFrom(operationIdentifier, currentTimestamp);
        }
        return currentTimestamp;
    }

    /** Adds a suffix indicating if a CPU or GPU was used. */
    private static String addSuffix(String string, boolean gpu) {
        return String.format("%s (%s)", string, gpu ? "GPU" : "CPU");
    }
}
