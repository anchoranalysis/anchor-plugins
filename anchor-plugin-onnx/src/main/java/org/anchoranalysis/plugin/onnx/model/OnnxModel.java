/*-
 * #%L
 * anchor-plugin-onnx
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
package org.anchoranalysis.plugin.onnx.model;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.Result;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedFunction;
import org.anchoranalysis.image.inference.ImageInferenceModel;

/**
 * A model that can be used for inference using the <a href="https://onnxruntime.ai/">ONNX
 * Runtime</a>'s Java API.
 *
 * <p>Note that a temporary directory is created by the ONNX Runtime, something ala {@code
 * C:\Users\owen\AppData\Local\Temp\onnxruntime-java3819764023069624084} with the final number
 * changing. This should be deleted after the Java VM closes, but this doesn't seem to always
 * happen. This requires further investigation, but can cause up a large buildup of files, as each
 * instance is approximately 300MBs.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
public class OnnxModel implements ImageInferenceModel<OnnxTensor> {

    // This session should be closed when no longer used.
    private OrtSession session;

    @Override
    public <S> S performInference(
            OnnxTensor input,
            String inputName,
            List<String> outputNames,
            CheckedFunction<List<OnnxTensor>, S, OperationFailedException> convertFunction)
            throws OperationFailedException {

        Map<String, OnnxTensor> inputs = new HashMap<>();
        inputs.put(inputName, input);

        try {
            try (Result result = session.run(inputs)) {
                List<OnnxTensor> tensors = tensorsFromResult(result, outputNames);
                return convertFunction.apply(tensors);
            }
        } catch (OrtException e) {
            throw new OperationFailedException("Inference failed with the ONNX Runtime", e);
        }
    }

    private static List<OnnxTensor> tensorsFromResult(Result result, List<String> outputNames)
            throws OperationFailedException {
        List<OnnxTensor> out = new ArrayList<>(outputNames.size());
        for (String identifier : outputNames) {
            OnnxTensor resultTensor =
                    (OnnxTensor)
                            result.get(identifier)
                                    .orElseThrow(
                                            () ->
                                                    new OperationFailedException(
                                                            String.format(
                                                                    "No output tensor with name '%s' exists",
                                                                    identifier)));
            out.add(resultTensor);
        }
        return out;
    }

    @Override
    public void close() throws OperationFailedException {
        try {
            session.close();
        } catch (Exception e) {
            throw new OperationFailedException("An error occured closing a Onnx session", e);
        } finally {
            session = null;
        }
    }
}
