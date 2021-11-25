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

import java.util.Optional;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;

/**
 * Records the execution-time of several operations during inference.
 *
 * <p>It expects all methods to be called once, in a defined order.
 *
 * <ol>
 *   <li>{@link #recordPre()}
 *   <li>{@link #recordInference()}
 *   <li>{@link #recordPost()}
 * </ol>
 *
 * @author Owen Feehan
 */
class InferenceExecutionTimeRecorder {

    /** Unique identifier for recording post-processing operations after model inference. */
    private static final String IDENTIFIER_POST = "Post-processing Inference";

    /** The underlying {@link ExecutionTimeRecorder} used for recording times. */
    private final ExecutionTimeRecorder recorder;

    /** Unique identifier for recording pre-processing operations before model inference. */
    private final String identifierPre;

    /**
     * Unique identifier for recording inference operation during the GPU warm-up (if it occurs).
     */
    private final Optional<String> identifierInferenceWarmUp;

    /** Unique identifier for recording subsequent inference operations, CPU or GPU. */
    private final String identifierInferenceSubsequent;

    /** A timestamp updated with the previous event. */
    private long timestamp;

    /**
     * Creates for a particular [@link ExecutionTimeRecorder}.
     *
     * @param recorder the recorder
     * @param gpu whether the inference uses a GPU or not
     */
    public InferenceExecutionTimeRecorder(ExecutionTimeRecorder recorder, boolean gpu) {
        this.recorder = recorder;
        this.identifierPre = addSuffix("Pre-processing Inference", gpu);
        this.identifierInferenceWarmUp =
                OptionalFactory.create(gpu, "Model Inference (GPU with Warm Up)");
        this.identifierInferenceSubsequent = addSuffix("Model Inference", gpu);
        this.timestamp = System.currentTimeMillis();
    }

    /** Record the current time as the end of pre-processing, and the start of inference. */
    public void recordPre() {
        this.timestamp = recorder.recordTimeDifferenceFrom(identifierPre, timestamp);
    }

    /** Record the current time as the end of inference, and the start of post-processing. */
    public void recordInference() {
        this.timestamp =
                recorder.recordTimeDifferenceFrom(
                        identifierInferenceSubsequent, identifierInferenceWarmUp, timestamp);
    }

    /** Record the current time as the end of post-processing. */
    public void recordPost() {
        recorder.recordTimeDifferenceFrom(IDENTIFIER_POST, timestamp);
    }

    /** Adds a suffix indicating if a CPU or GPU was used. */
    private static String addSuffix(String string, boolean gpu) {
        return String.format("%s (%s)", string, gpu ? "GPU" : "CPU");
    }
}
