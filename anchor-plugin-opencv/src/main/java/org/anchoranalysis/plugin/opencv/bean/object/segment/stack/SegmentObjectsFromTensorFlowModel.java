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
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.io.manifest.file.TextFileReader;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance.DecodeInstanceSegmentation;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import org.opencv.core.Mat;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

/**
 * Performs instance-segmentation, resulting in {@link ObjectMask}s using a TensorFlow model.
 * 
 * @author Owen Feehan
 *
 */
public class SegmentObjectsFromTensorFlowModel extends SegmentStackIntoObjectsPooled<Net> {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

    // START BEAN PROPERTIES
    /**
     * Relative-path to the TensorFlow model file, likely with <code>.pb</code> extension, relative to the <i>models/</i> directory in the Anchor distribution.
     */
    @BeanField @Getter @Setter private String modelBinaryPath;
    
    /**
     * Relative-path to the TensorFlow model file, likely with <code>.pb.txt</code> extension, relative to the <i>models/</i> directory in the Anchor distribution.
     * 
     * <p>If empty, then no such file is specified. 
     */
    @BeanField @Getter @Setter @AllowEmpty private String modelTextGraphPath = "";
    
    /**
     * Relative-path to the class-labels file, a text file where each line specifies a class label in order, relative to the <i>models/</i> directory in the Anchor distribution.
     * 
     * <p>If empty, then no such file is specified. 
     */
    @BeanField @Getter @Setter @AllowEmpty private String classLabelsPath = "";
    
    /**
     * Any scaling to be applied to the input-image before being input to the model for inference.
     */
    @BeanField @Getter @Setter private ScaleCalculator scaleInput;
    
    /**
     * Decodes inference output into segmented objects.
     */
    @BeanField @Getter @Setter private DecodeInstanceSegmentation decode;
    // END BEAN PROPERTIES

    @Override
    public ConcurrentModelPool<Net> createModelPool(ConcurrencyPlan plan) {
        return new ConcurrentModelPool<>(plan, this::createNet);
    }

    @Override
    public SegmentedObjects segment(Stack stack, ConcurrentModelPool<Net> modelPool)
            throws SegmentationFailedException {

        stack = checkAndCorrectInput(stack);

        try {
            ScaleFactor downfactor =
                    scaleInput.calculate(Optional.of(stack.dimensions()), Optional.empty());

            // Scales the input to the largest acceptable-extent
            Tuple2<Mat, ScaleFactor> pair = CreateScaledInput.apply(stack, downfactor, false);

            ScaleFactor upfactor = pair._2().invert();

            return decode.segmentMat(pair._1(), stack.resolution(), stack.extent(), upfactor, modelPool, classLabels());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SegmentationFailedException(e);
        } catch (Throwable e) {
            throw new SegmentationFailedException(e);
        }
    }

    private Net createNet(boolean useGPU) {

        Path model = resolve(modelBinaryPath);

        Optional<Path> textGraph = OptionalFactory.create(modelTextGraphPath).map(this::resolve);

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
    
    /** A list of ordered object-class labels, if a class-labels file is specified. */
    private Optional<List<String>> classLabels() throws IOException {
        if (!classLabelsPath.isEmpty()) {
            Path filename = resolve(classLabelsPath);
            return Optional.of(TextFileReader.readLinesAsList(filename));
        } else {
            return Optional.empty();
        }
    }

    /** Resolves a relative-filename (to the model directory) into a path. */
    private Path resolve(String filename) {
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
