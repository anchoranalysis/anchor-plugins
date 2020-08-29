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

import io.vavr.Tuple2;
import java.nio.file.Path;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.segment.object.SegmentStackIntoObjects;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.plugin.opencv.nonmaxima.NonMaximaSuppressionObjects;
import org.anchoranalysis.plugin.opencv.nonmaxima.WithConfidence;
import org.opencv.core.Mat;

/**
 * Extracts text from a RGB image by using the EAST deep neural network model
 *
 * <p>Particular thanks to <a
 * href="https://www.pyimagesearch.com/2018/08/20/opencv-text-detection-east-text-detector/">Adrian
 * Rosebrock</a> whose tutorial was useful in applying this model
 *
 * @author Owen Feehan
 */
public class SegmentText extends SegmentStackIntoObjects {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

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

    /** Bounding boxes with IoU scores above this threshold are removed */
    @BeanField @Getter @Setter private double suppressIntersectionOverUnionAbove = 0.3;

    /** Iff TRUE, non-maxima-suppression is applied to filter the proposed bounding boxes */
    @BeanField @Getter @Setter private boolean suppressNonMaxima = true;
    // END BEAN PROPERTIES


    @Override
    public ObjectCollection segment(Stack stack) throws SegmentationFailedException {

        checkInput(stack);

        try {
            // Scales the input to the largest acceptable-extent
            Tuple2<Mat, ScaleFactor> pair =
                    CreateScaledInput.apply(stack, findLargestExtent(stack.extent()));

            // Convert marks to object-masks
            List<WithConfidence<ObjectMask>> objectsWithConfidence =
                    EastObjectsExtractor.apply(
                            pair._1(),
                            stack.dimensions().resolution(),
                            minConfidence,
                            pathToEastModel());

            // Scale each object-mask and extract as an object-collection
            return ScaleExtractObjects.apply(
                    maybeFilterList(objectsWithConfidence), pair._2(), stack.extent());
        } catch (OperationFailedException | CreateException e) {
            throw new SegmentationFailedException(e);
        }
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

    private void checkInput(Stack stack) throws SegmentationFailedException {

        if (stack.getNumberChannels() != 3) {
            throw new SegmentationFailedException(
                    String.format(
                            "Non-RGB stacks are not supported by this algorithm. This stack has %d channels.",
                            stack.getNumberChannels()));
        }

        if (stack.dimensions().z() > 1) {
            throw new SegmentationFailedException("z-stacks are not supported by this algorithm");
        }
    }

    private List<WithConfidence<ObjectMask>> maybeFilterList(
            List<WithConfidence<ObjectMask>> list) {
        if (suppressNonMaxima) {
            return new NonMaximaSuppressionObjects()
                    .apply(list, suppressIntersectionOverUnionAbove);
        } else {
            return list;
        }
    }

    private Path pathToEastModel() {
        return getInitializationParameters()
                .getModelDirectory()
                .resolve("frozen_east_text_detection.pb");
    }
}