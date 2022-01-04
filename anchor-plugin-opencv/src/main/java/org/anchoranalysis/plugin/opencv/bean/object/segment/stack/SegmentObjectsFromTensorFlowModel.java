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

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalFactory;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.inference.bean.segment.instance.SegmentStackIntoObjectsScaleDecode;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.inference.concurrency.ConcurrentModel;
import org.anchoranalysis.inference.concurrency.ConcurrentModelPool;
import org.anchoranalysis.inference.concurrency.CreateModelFailedException;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.plugin.opencv.convert.ConvertToMat;
import org.anchoranalysis.plugin.opencv.segment.OpenCVModel;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

/**
 * Performs instance-segmentation using OpenCV's DNN module and a TensorFlow {@code .pb} <a
 * href="https://www.tensorflow.org/guide/saved_model">SavedModel</a> file.
 *
 * <p>Optionally a {@code .pb.txt} file may accompany it.
 *
 * @author Owen Feehan
 */
public class SegmentObjectsFromTensorFlowModel
        extends SegmentStackIntoObjectsScaleDecode<Mat, OpenCVModel> {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

    // START BEAN PROPERTIES
    /**
     * Relative-path to the TensorFlow model file, likely with <code>.pb</code> extension, relative
     * to the <i>models/</i> directory in the Anchor distribution.
     */
    @BeanField @Getter @Setter private String modelBinaryPath;

    /**
     * Relative-path to the TensorFlow model file, likely with <code>.pb.txt</code> extension,
     * relative to the <i>models/</i> directory in the Anchor distribution.
     *
     * <p>If empty, then no such file is specified.
     */
    @BeanField @Getter @Setter @AllowEmpty private String modelTextGraphPath = "";
    // END BEAN PROPERTIES

    @Override
    public ConcurrentModelPool<OpenCVModel> createModelPool(ConcurrencyPlan plan, Logger logger)
            throws CreateModelFailedException {
        // We disable all GPU inference as the current OpenCV library (from org.openpnp) does not
        // support it.

        // Note an alternative library JavaCPP from bytedeco does support CUDA inference with OpenCV
        // when one uses opencv-platform-gpu instead of opencv-platform. However, the presence of
        // opencv-platform-gpu in the classpath creates problems (stalling in CVInit) when a
        // CUDA-supported GPU isn't present.
        //
        // This means this possibility isn't particularly attractive, so for now, it is decided to
        // exclude GPU support from our OpenCV implementations.

        if (plan.numberGPUs() > 0) {
            logger.messageLogger()
                    .logFormatted(
                            "Although the plan allows for %d GPU processors, GPU processers are disabled with the OpenCV processor.",
                            plan.numberGPUs());
        }
        return new ConcurrentModelPool<>(plan.disableGPUs(), this::readPrepareModel, logger);
    }

    @Override
    protected Mat deriveInput(Stack stack, Optional<double[]> subtractMeans)
            throws OperationFailedException {
        // Scales the input to the largest acceptable-extent
        double[] toSubtract =
                subtractMeans.orElseGet(() -> arrayWithZeros(stack.getNumberChannels()));
        try {
            Mat mat = ConvertToMat.makeRGBStack(stack, false);
            return Dnn.blobFromImage(mat, 1.0, mat.size(), new Scalar(toSubtract), false, false);
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    protected Optional<String> inputName() {
        return Optional.empty();
    }

    /**
     * Reads the neural-network model from the file-system, and flags whether to use a GPU or not.
     *
     * @param useGPU if true, use a GPU if available, otherwise always use a CPU when performing
     *     inference with the model.
     * @return a newly created model
     */
    private Optional<ConcurrentModel<OpenCVModel>> readPrepareModel(boolean useGPU)
            throws CreateModelFailedException {

        try {
            Path model = resolve(modelBinaryPath);

            Optional<Path> textGraph =
                    OptionalUtilities.map(
                            OptionalFactory.create(modelTextGraphPath), this::resolve);

            CVInit.blockUntilLoaded();

            Net net = readNet(model, textGraph);

            if (useGPU) {
                // No exceptions will be thrown here immediately if CUDA support is unavailalbve
                // rather later when inference is first tried.
                net.setPreferableBackend(Dnn.DNN_BACKEND_CUDA);
                net.setPreferableTarget(Dnn.DNN_TARGET_CUDA);
            }
            return Optional.of(new ConcurrentModel<>(new OpenCVModel(net), useGPU));

        } catch (InitializeException e) {
            throw new CreateModelFailedException(e);
        }
    }

    /**
     * Reads the CNN model from the file-system in Tensorflow format.
     *
     * @param model a Tensorflow model file in {@code .pb} format.
     * @param textGraph a Tensorflow text graph in {@code .pbtxt} format.
     * @return the loaded CNN model.
     */
    private static Net readNet(Path model, Optional<Path> textGraph) {

        if (textGraph.isPresent()) {
            return Dnn.readNetFromTensorflow(absolutePath(model), absolutePath(textGraph.get()));
        } else {
            return Dnn.readNetFromTensorflow(absolutePath(model));
        }
    }

    /** Converts a {@link Path} into an absolute-path encoded as a string. */
    private static String absolutePath(Path path) {
        return path.toAbsolutePath().toString();
    }

    /** Creates an array containing only zeros. */
    private static double[] arrayWithZeros(int size) {
        return new double[size];
    }
}
