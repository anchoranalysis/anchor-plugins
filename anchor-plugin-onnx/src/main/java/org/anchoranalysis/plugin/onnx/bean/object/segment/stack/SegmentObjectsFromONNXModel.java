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
package org.anchoranalysis.plugin.onnx.bean.object.segment.stack;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtLoggingLevel;
import ai.onnxruntime.OrtProvider;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.SessionOptions;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.inference.bean.segment.instance.SegmentStackIntoObjectsScaleDecode;
import org.anchoranalysis.image.voxel.interpolator.Interpolator;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.inference.concurrency.ConcurrentModel;
import org.anchoranalysis.inference.concurrency.ConcurrentModelPool;
import org.anchoranalysis.inference.concurrency.CreateModelFailedException;
import org.anchoranalysis.io.imagej.iterpolator.InterpolatorImageJ;
import org.anchoranalysis.plugin.onnx.model.OnnxModel;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import org.apache.commons.io.IOUtils;

/**
 * Performs instance-segmentation using the ONNX Runtime and an {@code .onnx} model file.
 *
 * @author Owen Feehan
 */
public class SegmentObjectsFromONNXModel
        extends SegmentStackIntoObjectsScaleDecode<OnnxTensor, OnnxModel> {

    // This is used for downscaling as it's fast.
    private static final Interpolator INTERPOLATOR = new InterpolatorImageJ();

    // START BEAN PROPERTIES
    /**
     * Relative-path to the model file in ONNX form, relative to the <i>models/</i> directory in the
     * Anchor distribution.
     * 
     * <p>If {@code readFromResources==true}, it is read instead from resources on the class-path.
     */
    @BeanField @Getter @Setter private String modelPath;
    
    /**
     * When true, rather than reading {@code modelPath} from the file-system, it is read from Java
     * resources on the class-path.
     */
    @BeanField @Getter @Setter private boolean readFromResources = false;

    /** The name of the input in the ONNX model. */
    @BeanField @Getter @Setter private String inputName;

    /**
     * If true, a 4-dimensional tensor is created (with the first dimension describing a batch-size
     * of 1), instead of the usual 3-dimensional tensor describing channel, height, width.
     */
    @BeanField @Getter @Setter private boolean includeBatchDimension = false;

    /**
     * If true, the channels are placed as the final position of the tensor (**after** width/height)
     * instead of **before** width/height.
     *
     * <p>Consequently, in terms of raw order in a {@link FloatBuffer}, RGB values become
     * interleaved.
     */
    @BeanField @Getter @Setter private boolean interleaveChannels = false;
    // END BEAN PROPERTIES
    
    /** The model read from the file-system as bytes. */
    private byte[] modelAsBytes;

    @Override
    public ConcurrentModelPool<OnnxModel> createModelPool(ConcurrencyPlan plan, Logger logger)
            throws CreateModelFailedException {
        return new ConcurrentModelPool<>(plan, this::readPrepareModel, logger);
    }

    @Override
    protected OnnxTensor deriveInput(
            Stack stack, ScaleFactor downfactor, Optional<double[]> subtractMeans)
            throws OperationFailedException {

        stack = stack.mapChannel(channel -> channel.scaleXY(downfactor, INTERPOLATOR));

        // Change from RGB to BGR
        try {
            stack = new Stack(false, stack.getChannel(2), stack.getChannel(1), stack.getChannel(0));
        } catch (IncorrectImageSizeException | CreateException e1) {
            throw new AnchorImpossibleSituationException();
        }

        FloatBuffer bufferTensor =
                BufferFromStack.createFrom(stack, subtractMeans, interleaveChannels);

        // Needed otherwise createTensor doesn't work.
        bufferTensor.rewind();

        try {
            return OnnxTensor.createTensor(
                    OrtEnvironment.getEnvironment(), bufferTensor, deriveShape(stack));
        } catch (OrtException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    protected Optional<String> inputName() {
        return Optional.of(getInputName());
    }

    /**
     * Enables CUDA in a session, if possible, catching exceptions that may occur when not possible.
     *
     * @param options the options to configure CUDA on.
     * @return true if CUDA was successfully configured on {@code options}, otherwise false.
     */
    private boolean configureCUDAIfPossible(SessionOptions options) {
        if (OrtEnvironment.getAvailableProviders().contains(OrtProvider.CUDA)) {
            try {
                options.addCUDA();
                return true;
            } catch (OrtException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Reads the neural-network model from the file-system, and flags whether to use a GPU or not.
     *
     * @param useGPU if true, use a GPU if available, otherwise always use a CPU when performing
     *     inference with the model.
     * @return a newly created model, if possible.
     * @throws CreateModelFailedException if an unexpected error occurred, but not if no GPU is
     *     unavailable, which should be returned as {@link Optional#empty}.
     */
    private Optional<ConcurrentModel<OnnxModel>> readPrepareModel(boolean useGPU)
            throws CreateModelFailedException {

        try {
            // Using this logging-level is important to suppress log messages sent to standard-error
            //  when the import of CUDA fails.
            OrtEnvironment env =
                    OrtEnvironment.getEnvironment(OrtLoggingLevel.ORT_LOGGING_LEVEL_FATAL);

            SessionOptions options = new OrtSession.SessionOptions(); // NOSONAR
            if (useGPU && !configureCUDAIfPossible(options)) {
                options.close();
                return Optional.empty();
            }
            
            OrtSession session = env.createSession(readModelIfNecessary(), options); // NOSONAR

            return Optional.of(new ConcurrentModel<>(new OnnxModel(session), useGPU));

        } catch (InitializeException | OrtException | IOException e) {
            throw new CreateModelFailedException(e);
        }
    }

    /**
     * Describes the shape of the input-tensor in the form (channel, size-y, size-x) that the ONNX
     * Runtime expects.
     */
    private long[] deriveShape(Stack stack) {
        Dimensions dimensions = stack.getChannel(0).dimensions();
        if (includeBatchDimension) {
            return new long[] {1, dimensions.y(), dimensions.x(), stack.getNumberChannels()};
        } else {
            return new long[] {stack.getNumberChannels(), dimensions.y(), dimensions.x()};
        }
    }
    
    /** Reads the ONNX model as a byte-array, either from the file-system or from resources. */
    private byte[] readModelIfNecessary() throws IOException, InitializeException {
        if (modelAsBytes==null) {
            if (readFromResources) {
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                InputStream inputStream = classloader.getResourceAsStream(modelPath);
                modelAsBytes = IOUtils.toByteArray(inputStream);
            } else {
                Path path = resolve(modelPath);
                modelAsBytes = Files.readAllBytes(path);
            }
        }
        return modelAsBytes;
    }
}
