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

import io.vavr.Tuple2;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.primitive.DoubleList;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.system.ExecutionTimeRecorder;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.inference.concurrency.ConcurrentModel;
import org.anchoranalysis.inference.concurrency.ConcurrentModelPool;
import org.anchoranalysis.inference.concurrency.CreateModelFailedException;
import org.anchoranalysis.io.manifest.file.TextFileReader;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance.DecodeInstanceSegmentation;
import org.anchoranalysis.plugin.opencv.segment.InferenceContext;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import org.apache.commons.collections.IteratorUtils;
import org.opencv.core.Mat;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

/**
 * Performs instance-segmentation, resulting in {@link ObjectMask}s using a TensorFlow model.
 *
 * @author Owen Feehan
 */
public class SegmentObjectsFromTensorFlowModel extends SegmentStackIntoObjectsPooled<Net> {

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

    /**
     * Relative-path to the class-labels file, a text file where each line specifies a class label
     * in order, relative to the <i>models/</i> directory in the Anchor distribution.
     *
     * <p>If empty, then no such file is specified.
     */
    @BeanField @Getter @Setter @AllowEmpty private String classLabelsPath = "";

    /**
     * Any scaling to be applied to the input-image before being input to the model for inference.
     */
    @BeanField @Getter @Setter private ScaleCalculator scaleInput;

    /** Decodes inference output into segmented objects. */
    @BeanField @Getter @Setter private DecodeInstanceSegmentation decode;

    /**
     * A constant intensity for each respective channel to be subtracted before performing
     * inference.
     *
     * <p>If set, this should create an list, with as many elements as channels inputted to the
     * inference model.
     */
    @BeanField @OptionalBean @Getter @Setter private DoubleList subtractMean;
    // END BEAN PROPERTIES

    @Override
    public ConcurrentModelPool<Net> createModelPool(ConcurrencyPlan plan)
            throws CreateModelFailedException {
        // We disable all GPU inference as the current OpenCV library (from org.openpnp) does not
        // support it

        // Note an alternative library JavaCPP from bytedeco does support CUDA inference with OpenCV
        // when one
        // uses opencv-platform-gpu instead of opencv-platform. However, the presence of
        // opencv-platform-gpu
        // in the classpath creates problems (stalling in CVInit) when a CUDA-supported GPU isn't
        // present.
        // This means this possibility isn't particularly attractive, so for now, it is decided to
        // exclude GPU
        // support from our OpenCV implementations.
        return new ConcurrentModelPool<>(plan.disableGPUs(), this::readPrepareModel);
    }

    @Override
    public SegmentedObjects segment(
            Stack stack,
            ConcurrentModelPool<Net> modelPool,
            ExecutionTimeRecorder executionTimeRecorder)
            throws SegmentationFailedException {

        stack = checkAndCorrectInput(stack);

        try {
            ScaleFactor downfactor =
                    scaleInput.calculate(Optional.of(stack.dimensions()), Optional.empty());

            // Scales the input to the largest acceptable-extent
            Tuple2<Mat, ScaleFactor> pair = CreateScaledInput.apply(stack, downfactor, false);

            ScaleFactor upfactor = pair._2().invert();

            InferenceHelper helper =
                    new InferenceHelper(decode, subtractMeanArray(stack.getNumberChannels()));
            return helper.queueInference(
                    pair._1(),
                    modelPool,
                    new InferenceContext(
                            stack.dimensions(),
                            upfactor,
                            classLabels(),
                            executionTimeRecorder,
                            getInitialization().getSharedObjects().getContext().getLogger()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SegmentationFailedException(e);
        } catch (Throwable e) {
            throw new SegmentationFailedException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private double[] subtractMeanArray(int numberChannels) throws SegmentationFailedException {

        if (subtractMean != null) {
            List<Double> list = (List<Double>) IteratorUtils.toList(subtractMean.iterator());

            if (list.size() != numberChannels) {
                throw new SegmentationFailedException(
                        String.format(
                                "There are %d channels in the input stack for inference, but %d constants were supplied for mean-subtraction.",
                                numberChannels, list.size()));
            }

            double[] out = new double[list.size()];
            for (int i = 0; i < out.length; i++) {
                out[i] = list.get(i);
            }
            return out;
        } else {
            double[] out = new double[numberChannels];
            for (int i = 0; i < out.length; i++) {
                out[i] = 0.0;
            }
            return out;
        }
    }

    /**
     * Reads the neural-network model from the file-system, and flags whether to use a GPU or not.
     *
     * @param useGPU if true, use a GPU if available, otherwise always use a CPU when performing
     *     inference with the model.
     * @return a newly created model
     */
    private ConcurrentModel<Net> readPrepareModel(boolean useGPU)
            throws CreateModelFailedException {

        try {
            Path model = resolve(modelBinaryPath);

            Optional<Path> textGraph =
                    OptionalUtilities.map(
                            OptionalFactory.create(modelTextGraphPath), this::resolve);

            CVInit.blockUntilLoaded();

            Net net = readNet(model, textGraph);

            if (useGPU) {
                net.setPreferableBackend(Dnn.DNN_BACKEND_CUDA);
                net.setPreferableTarget(Dnn.DNN_TARGET_CUDA);
            }
            return new ConcurrentModel<>(net, useGPU);

        } catch (InitializeException e) {
            throw new CreateModelFailedException(e);
        }
    }

    /**
     * Reads the CNN model from the file-system in Tensorflow format.
     *
     * @param model a Tensorflow model file in .pb format
     * @param textGraph a Tensorflow text graph in .pbtxt format
     * @return the loaded CNN model.
     */
    private static Net readNet(Path model, Optional<Path> textGraph) {

        if (textGraph.isPresent()) {
            return Dnn.readNetFromTensorflow(absolutePath(model), absolutePath(textGraph.get()));
        } else {
            return Dnn.readNetFromTensorflow(absolutePath(model));
        }
    }

    /** Checks the input-stack has the necessary number of channels, otherwise throwing an error. */
    private Stack checkAndCorrectInput(Stack stack) throws SegmentationFailedException {
        if (stack.getNumberChannels() == 1) {
            return checkInput(grayscaleToRGB(stack.getChannel(0)));
        } else {
            return checkInput(stack);
        }
    }

    private Stack checkInput(Stack stack) throws SegmentationFailedException {
        if (stack.getNumberChannels() != 3) {
            throw new SegmentationFailedException(
                    String.format(
                            "Non-RGB stacks are not supported by this algorithm. This stack has %d channels.",
                            stack.getNumberChannels()));
        }

        if (stack.dimensions().z() > 1) {
            throw new SegmentationFailedException("z-stacks are not supported by this algorithm");
        }

        return stack;
    }

    /** A list of ordered object-class labels, if a class-labels file is specified. */
    private Optional<List<String>> classLabels() throws IOException {
        if (!classLabelsPath.isEmpty()) {
            try {
                Path filename = resolve(classLabelsPath);
                return Optional.of(TextFileReader.readLinesAsList(filename));
            } catch (InitializeException e) {
                throw new IOException(e);
            }
        } else {
            return Optional.empty();
        }
    }

    /** Resolves a relative-filename (to the model directory) into a path. */
    private Path resolve(String filename) throws InitializeException {
        return getInitialization().getModelDirectory().resolve(filename);
    }

    /** Converts a {@link Path} into an absolute-path encoded as a string. */
    private static String absolutePath(Path path) {
        return path.toAbsolutePath().toString();
    }

    private static Stack grayscaleToRGB(Channel channel) {
        try {
            return new Stack(true, channel, channel.duplicate(), channel.duplicate());
        } catch (IncorrectImageSizeException e) {
            throw new AnchorImpossibleSituationException();
        }
    }
}
