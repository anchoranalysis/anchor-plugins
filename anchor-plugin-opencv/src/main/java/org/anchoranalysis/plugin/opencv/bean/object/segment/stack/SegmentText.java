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
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import org.opencv.core.Mat;
import org.opencv.dnn.Dnn;
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
public class SegmentText extends SegmentStackIntoObjectsPooled<Net> {

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
    // END BEAN PROPERTIES

    @Override
    public SegmentedObjects segment(Stack stack, ConcurrentModelPool<Net> modelPool)
            throws SegmentationFailedException {

        stack = checkAndCorrectInput(stack);

        try {
            // Scales the input to the largest acceptable-extent
            Tuple2<Mat, ScaleFactor> pair =
                    CreateScaledInput.apply(stack, findLargestExtent(stack.extent()));

            // Convert marks to object-masks
            SegmentedObjects objects =
                    EastObjectsExtracter.apply(
                            modelPool, pair._1(), stack.resolution(), minConfidence);

            // Scale each object-mask and extract as an object-collection
            return objects.scale(pair._2(), stack.extent());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SegmentationFailedException(e);
        } catch (Throwable e) {
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

    private Stack checkAndCorrectInput(Stack stack) throws SegmentationFailedException {
        if (stack.getNumberChannels() == 1) {
            return checkInput(grayscaleToRGB(stack.getChannel(0)));
        } else {
            return checkInput(stack);
        }
    }

    private static Stack grayscaleToRGB(Channel channel) {
        try {
            return new Stack(true, channel, channel.duplicate(), channel.duplicate());
        } catch (IncorrectImageSizeException e) {
            throw new AnchorImpossibleSituationException();
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

    @Override
    public ConcurrentModelPool<Net> createModelPool(ConcurrencyPlan plan) {
        Path modelPath = pathToEastModel();
        return new ConcurrentModelPool<>(plan, useGPU -> createNet(modelPath, useGPU));
    }

    private static Net createNet(Path pathToModel, boolean useGPU) {

        CVInit.blockUntilLoaded();

        Net net = Dnn.readNetFromTensorflow(pathToModel.toAbsolutePath().toString());

        if (useGPU) {
            net.setPreferableBackend(Dnn.DNN_BACKEND_CUDA);
            net.setPreferableTarget(Dnn.DNN_TARGET_CUDA);
        }
        return net;
    }

    private Path pathToEastModel() {
        return getInitialization()
                .getModelDirectory()
                .resolve("frozen_east_text_detection.pb");
    }
}
