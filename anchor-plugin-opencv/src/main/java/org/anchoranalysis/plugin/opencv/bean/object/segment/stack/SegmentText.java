/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import org.opencv.core.Mat;
import org.opencv.dnn.Net;

/**
 * Extracts text from a RGB image by using the EAST deep neural network model
 *
 * <p>Particular thanks to <a
 * href="https://www.pyimagesearch.com/2018/08/20/opencv-text-detection-east-text-detector/">Adrian
 * Rosebrock</a> whose tutorial was useful in applying this model
 *
 * @author Owen Feehan
 */
public class SegmentText extends SegmentFromTensorFlowModel {

    /** Only exact integral multiples of this size in each dimension can be accepted as input */
    private static final Extent EAST_EXTENT = new Extent(32, 32);

    /**
     * As the EAST detector was designed to work with originally 1280x720 pixel images approximately
     * we don't allow dramatically higher resolutions that this, so text objects remain roughly in
     * size proportionate to what EAST was trained on.
     */
    private static final int MAX_SCALE_FACTOR = (720 / EAST_EXTENT.y());

    // START BEAN PROPERTIES
    /** Proposed bounding boxes below this confidence interval are removed */
    @BeanField @Getter @Setter private double minConfidence = 0.5;
    // END BEAN PROPERTIES

    @Override
    protected SegmentedObjects segmentMat(
            Mat mat,
            Optional<Resolution> resolution,
            Extent unscaledSize,
            ScaleFactor scaleFactor,
            ConcurrentModelPool<Net> modelPool)
            throws Throwable {
        // Convert marks to object-masks
        SegmentedObjects objects =
                EastObjectsExtracter.apply(modelPool, mat, resolution, minConfidence);

        // Scale each object-mask and extract as an object-collection
        return objects.scale(scaleFactor, unscaledSize);
    }

    @Override
    protected Extent inputSizeForModel(Extent imageSize) throws CreateException {
        return findLargestExtent(imageSize);
    }

    @Override
    protected String modelPath() {
        return "frozen_east_text_detection.pb";
    }

    @Override
    protected Optional<String> textGraphPath() {
        return Optional.empty();
    }

    /**
     * Finds largest allowed extent to scale the input image down to
     *
     * @param stayWithin an upper bound on what's allowed
     * @return the largest extent allowed that is a scale multiple of EAST_EXTENT
     * @throws CreateException
     */
    private static Extent findLargestExtent(Extent stayWithin) throws CreateException {
        try {
            return FindLargestMultipleWithin.apply(EAST_EXTENT, stayWithin, MAX_SCALE_FACTOR);
        } catch (OperationFailedException e) {
            throw new CreateException("Cannot scale input to size needed for EAST", e);
        }
    }
}
