package org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import org.opencv.core.Mat;
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
     * @param resolution the image-resolution of {@code mat} if it exists.
     * @param unscaledSize the size of the image before any scaling.
     * @param scaleFactor the scaling factor to scale objects to their original size.
     * @param modelPool the models used for CNN inference
     * @param classLabels if available, labels of classes loaded from a text file at {@code classLabelsPath}.
     * @return the results of the segmentation
     * @throws Throwable
     */
    public abstract SegmentedObjects segmentMat(
            Mat mat,
            Optional<Resolution> resolution,
            Extent unscaledSize,
            ScaleFactor scaleFactor,
            ConcurrentModelPool<Net> modelPool, Optional<List<String>> classLabels)
            throws Throwable; // NOSONAR
}
