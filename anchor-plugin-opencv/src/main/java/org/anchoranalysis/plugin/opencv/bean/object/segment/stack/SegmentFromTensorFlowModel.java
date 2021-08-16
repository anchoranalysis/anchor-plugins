package org.anchoranalysis.plugin.opencv.bean.object.segment.stack;

import io.vavr.Tuple2;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import org.opencv.core.Mat;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

public abstract class SegmentFromTensorFlowModel extends SegmentStackIntoObjectsPooled<Net> {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

    @Override
    public ConcurrentModelPool<Net> createModelPool(ConcurrencyPlan plan) {
        return new ConcurrentModelPool<>(plan, this::createNet);
    }

    @Override
    public SegmentedObjects segment(Stack stack, ConcurrentModelPool<Net> modelPool)
            throws SegmentationFailedException {

        stack = checkAndCorrectInput(stack);

        try {
            // Scales the input to the largest acceptable-extent
            Tuple2<Mat, ScaleFactor> pair =
                    CreateScaledInput.apply(stack, inputSizeForModel(stack.extent()), false);

            return segmentMat(pair._1(), stack.resolution(), stack.extent(), pair._2(), modelPool);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SegmentationFailedException(e);
        } catch (Throwable e) {
            throw new SegmentationFailedException(e);
        }
    }

    /**
     * Segment the image contained in {@code mat}.
     *
     * @param mat an OpenCV matrix containing the image to be segmented, already resized to {@link
     *     #inputSizeForModel}.
     * @param resolution the image-resolution of {@code mat} if it exists.
     * @param unscaledSize the size of the image before any scaling to {@link #inputSizeForModel}.
     * @param scaleFactor the scaling factor used to transform {@code unscaledSize} to {@link
     *     #inputSizeForModel}.
     * @param modelPool the models used for CNN inference
     * @return the results of the segmentation
     * @throws Throwable
     */
    protected abstract SegmentedObjects segmentMat(
            Mat mat,
            Optional<Resolution> resolution,
            Extent unscaledSize,
            ScaleFactor scaleFactor,
            ConcurrentModelPool<Net> modelPool)
            throws Throwable; // NOSONAR

    /**
     * The size of the image when passed as an input to the CNN Model.
     *
     * @param imageSize the size of the original image before any rescaling for the CNN model.
     * @return the size the image should be resized to when inputted to thei masge
     * @throws CreateException
     */
    protected abstract Extent inputSizeForModel(Extent imageSize) throws CreateException;

    /**
     * The relative path to the TensorFlow model file.
     *
     * @return a path to the model file, relative to the <i>models/</i> subdirectory in the
     *     resources.
     */
    protected abstract String modelPath();

    /**
     * The relative path to the TensorFlow text-graph file, if it exists.
     *
     * @return a path to the text-graph file, relative to the <i>models/</i> subdirectory in the
     *     resources.
     */
    protected abstract Optional<String> textGraphPath();

    private Net createNet(boolean useGPU) {

        Path model = resolve(modelPath());

        Optional<Path> textGraph = textGraphPath().map(this::resolve);

        CVInit.blockUntilLoaded();

        Net net = readNet(model, textGraph);

        if (useGPU) {
            net.setPreferableBackend(Dnn.DNN_BACKEND_CUDA);
            net.setPreferableTarget(Dnn.DNN_TARGET_CUDA);
        }
        return net;
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

    /** Resolves a relative-filename (to the model directory) into a path. */
    protected Path resolve(String filename) {
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
