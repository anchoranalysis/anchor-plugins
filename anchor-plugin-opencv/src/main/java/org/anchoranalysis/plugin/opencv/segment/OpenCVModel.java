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
package org.anchoranalysis.plugin.opencv.segment;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedFunction;
import org.anchoranalysis.image.inference.ImageInferenceModel;
import org.opencv.core.Mat;
import org.opencv.dnn.Net;

/**
 * A model that can be used for inference using <a
 * href="https://docs.opencv.org/4.x/d2/d58/tutorial_table_of_content_dnn.html">OpenCV's DNN
 * module</a>.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
public class OpenCVModel implements ImageInferenceModel<Mat> {

    /** An OpenCV model used for inference. */
    private Net model;

    @Override
    public <S> S performInference(
            Mat input,
            String inputName,
            List<String> outputNames,
            CheckedFunction<List<Mat>, S, OperationFailedException> convertFunction)
            throws OperationFailedException {
        List<Mat> output = new ArrayList<>();
        model.setInput(input);
        model.forward(output, outputNames);
        return convertFunction.apply(output);
    }

    @Override
    public void close() {
        model = null;
    }
}
