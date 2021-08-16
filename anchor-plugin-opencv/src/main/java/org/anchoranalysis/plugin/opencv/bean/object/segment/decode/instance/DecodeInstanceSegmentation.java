package org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.concurrency.ConcurrentModelException;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.image.segment.LabelledWithConfidence;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

/**
 * Decodes inference output into segmented-objects.
 *  
 * @author Owen Feehan
 *
 */
public abstract class DecodeInstanceSegmentation extends AnchorBean<DecodeInstanceSegmentation> {

    /**
     * Performs inference, and decodes the {@link Mat} into segmented-objects.
     *
     * @param mat an OpenCV matrix containing the image to be segmented, already resized to the
     *     desired scale for inputting to the model for inference.
     * @param dimensions the image-resolution of {@code mat} if it exists.
     * @param unscaledSize the size of the image before any scaling.
     * @param scaleFactor the scaling factor to scale objects to their original size.
     * @param modelPool the models used for CNN inference
     * @param classLabels if available, labels of classes loaded from a text file at {@code classLabelsPath}.
     * @return the results of the segmentation
     * @throws Throwable
     */
    public abstract SegmentedObjects segmentMat(
            Mat mat,
            Dimensions dimensions,
            Extent unscaledSize,
            ScaleFactor scaleFactor,
            ConcurrentModelPool<Net> modelPool, Optional<List<String>> classLabels)
            throws Throwable; // NOSONAR
    
    /** Performs inference to generate the masks. */
    protected List<LabelledWithConfidence<ObjectMask>> queueInference(
            ConcurrentModelPool<Net> modelPool, Mat image, Extent unscaledSize, Optional<List<String>> classLabels, Dimensions inputDimensions) throws Throwable {
        return modelPool.excuteOrWait(model -> performInference(model, image, output -> decode(output, unscaledSize, classLabels, inputDimensions)));
    }    
    
    protected <T> T performInference(Net model, Mat image, Function<List<Mat>,T> processOutput) throws ConcurrentModelException {
        try {
            model.setInput(
                    Dnn.blobFromImage(
                            image, 1.0, image.size(), meanSubtractionConstants(), false, false));

            List<Mat> output = new ArrayList<>();
            model.forward(output, expectedOutputs());
            return processOutput.apply(output);
        } catch (Exception e) {
            throw new ConcurrentModelException(e);
        }
    }
    
    public abstract List<String> expectedOutputs();
    
    public abstract Scalar meanSubtractionConstants();
    
    /** Performs the inference on a single image using the CNN model. */
    protected abstract List<LabelledWithConfidence<ObjectMask>> decode(
            List<Mat> output, Extent unscaledSize, Optional<List<String>> classLabels, Dimensions inputDimensions);
}
