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
package org.anchoranalysis.plugin.onnx.bean.object.segment.decode.instance;

import ai.onnxruntime.OnnxTensor;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.inference.ImageInferenceContext;
import org.anchoranalysis.image.inference.bean.segment.instance.DecodeInstanceSegmentation;
import org.anchoranalysis.image.inference.segment.LabelledWithConfidence;
import org.anchoranalysis.image.inference.segment.ScaleAndThresholdVoxels;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBufferWrap;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.point.Point3d;
import org.anchoranalysis.spatial.point.PointConverter;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Decodes the inference output from a Mask-RCNN implementation.
 *
 * <p>It is designed to work with accompanying {@code MaskRCNN-10.onnx} in resources, which expects
 * an image of size 1088x800 (width x height) and may throw an error if the input-size is different
 * than this.
 *
 * <p>The ONNX file was obtained from <a
 * href="https://github.com/onnx/models/tree/master/vision/object_detection_segmentation/mask-rcnn">this
 * GitHub source</a>, which also describes its inputs and outputs.
 *
 * <p>This <a href="https://github.com/microsoft/onnxruntime/issues/1670">issue</a> may also be
 * relevant, discussing the error message that occurs when the size above is not used as the input.
 *
 * @author Owen Feehan
 */
public class DecodeMaskRCNN extends DecodeInstanceSegmentation<OnnxTensor> {

    private static final String OUTPUT_LABELS = "6570";

    private static final String OUTPUT_SCORES = "6572";

    /** Name of model output for encoded bounding-boxes. */
    private static final String OUTPUT_BOXES = "6568";

    /** Name of model output for object-masks. */
    private static final String OUTPUT_MASKS = "6887";

    /** We expect this to be the width and height of each produced mask. */
    private static final int WIDTH_HEIGHT_MASK = 28;

    /** We expect this to be number of pixels in each produced mask. */
    private static final int NUMBER_PIXELS_MASK = WIDTH_HEIGHT_MASK * WIDTH_HEIGHT_MASK;

    /** A fallback object-class-label used, if no class-labels are provided to index. */
    private static final String OBJECT_CLASS_LABEL_FALLBACK = "unknown";

    /** Scaler for the mask voxels. */
    private static final ScaleAndThresholdVoxels scaler = new ScaleAndThresholdVoxels(false);

    // START BEAN PROPERTIES
    /**
     * Only proposals outputted from the model with a score greater or equal to this threshold are
     * considered.
     */
    @BeanField @Getter @Setter private float minConfidence = 0.5f;

    /** Threshold above which pixels are considered in the mask. */
    @BeanField @Getter @Setter private float minMaskValue = 0.5f;
    // END BEAN PROPERTIES

    @Override
    public List<String> expectedOutputs() {
        return Arrays.asList(OUTPUT_BOXES, OUTPUT_LABELS, OUTPUT_SCORES, OUTPUT_MASKS);
    }

    @Override
    public List<LabelledWithConfidence<ObjectMask>> decode(
            List<OnnxTensor> inferenceOutput, ImageInferenceContext context)
            throws OperationFailedException {

        FloatBuffer scores = inferenceOutput.get(2).getFloatBuffer();
        int numberProposals = scores.capacity();

        List<Integer> indices = indicesAboveThreshold(scores);

        if (indices.isEmpty()) {
            return new ArrayList<>();
        }

        FloatBuffer masks = inferenceOutput.get(3).getFloatBuffer();
        if (masks.capacity() != (NUMBER_PIXELS_MASK * numberProposals)) {
            throw new OperationFailedException(
                    String.format(
                            "We expect %d number of pixels in each mask, but this isn't true.",
                            NUMBER_PIXELS_MASK));
        }

        LongBuffer labels = inferenceOutput.get(1).getLongBuffer();

        FloatBuffer boxes = inferenceOutput.get(0).getFloatBuffer();

        return FunctionalList.mapToListOptional(
                indices, index -> deriveObject(index, scores, masks, labels, boxes, context));
    }

    private Optional<LabelledWithConfidence<ObjectMask>> deriveObject(
            int index,
            FloatBuffer scores,
            FloatBuffer masks,
            LongBuffer labels,
            FloatBuffer boxes,
            ImageInferenceContext context) {
        BoundingBox box =
                boxAtIndex(
                        boxes, index, context.getDimensions().extent(), context.getScaleFactor());

        Optional<ObjectMask> objectMask =
                maskAtIndexAsObject(masks, index, box, context.getExecutionTimeRecorder());
        if (objectMask.isPresent()) {
            double score = scores.get(index);

            String label =
                    context.getClassLabels()
                            .map(labelList -> labelList.get(index))
                            .orElse(OBJECT_CLASS_LABEL_FALLBACK);

            return Optional.of(new LabelledWithConfidence<>(objectMask.get(), score, label));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Finds the indices of score entries that are above the threshold.
     *
     * @return a list of the indices of the elements above the threshold, in ascending index order.
     */
    private List<Integer> indicesAboveThreshold(FloatBuffer scores) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < scores.capacity(); i++) {
            if (scores.get(i) >= minConfidence) {
                indices.add(i);
            }
        }
        return indices;
    }

    private static BoundingBox boxAtIndex(
            FloatBuffer buffer, int boxIndex, Extent extent, ScaleFactor scaleFactor) {
        int indexMin = boxIndex * 4;
        Point3d min =
                new Point3d(
                        buffer.get(indexMin) * scaleFactor.x(),
                        buffer.get(indexMin + 1) * scaleFactor.y(),
                        0.0);
        Point3d max =
                new Point3d(
                        buffer.get(indexMin + 2) * scaleFactor.x(),
                        buffer.get(indexMin + 3) * scaleFactor.y(),
                        0.0);
        BoundingBox box =
                new BoundingBox(
                        PointConverter.intFromDouble(min, true),
                        PointConverter.intFromDouble(max, true));
        box = box.clampTo(extent);
        return box;
    }

    private Optional<ObjectMask> maskAtIndexAsObject(
            FloatBuffer buffer,
            int maskIndex,
            BoundingBox box,
            ExecutionTimeRecorder executionTimeRecorder) {

        float[] extractedMaskPixels = new float[NUMBER_PIXELS_MASK];

        VoxelBuffer<FloatBuffer> voxelBuffer = VoxelBufferWrap.floatArray(extractedMaskPixels);

        buffer.position(maskIndex * NUMBER_PIXELS_MASK);
        buffer.get(extractedMaskPixels);

        if (!hasPixelAboveThreshold(extractedMaskPixels)) {
            return Optional.empty();
        }

        Voxels<FloatBuffer> voxels =
                VoxelsFactory.getFloat()
                        .createForVoxelBuffer(
                                voxelBuffer, new Extent(WIDTH_HEIGHT_MASK, WIDTH_HEIGHT_MASK, 1));

        BinaryVoxels<UnsignedByteBuffer> scaledMask =
                executionTimeRecorder.recordExecutionTime(
                        "Scale and threshold mask",
                        () -> scaler.scaleAndThreshold(voxels, box.extent(), minMaskValue));

        return Optional.of(new ObjectMask(box, scaledMask));
    }

    /** Is at least one pixel above the threshold? */
    private boolean hasPixelAboveThreshold(float[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] >= minMaskValue) {
                return true;
            }
        }
        return false;
    }
}
