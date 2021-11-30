package org.anchoranalysis.plugin.onnx.bean.object.segment.decode.instance;

import org.anchoranalysis.bean.primitive.DoubleList;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.inference.bean.segment.instance.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.image.inference.bean.segment.instance.SuppressNonMaxima;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.ConditionallyMergeOverlappingObjects;
import org.anchoranalysis.plugin.image.bean.scale.LargestMultipleWithin;
import org.anchoranalysis.plugin.onnx.bean.object.segment.stack.SegmentObjectsFromONNXModel;
import org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance.SegmentNumberPlateBase;

/**
 * Tests {@link SegmentObjectsFromONNXModel} together with {@link DecodeText}.
 *
 * @author Owen Feehan
 */
class SegmentTextFromONNXTest extends SegmentNumberPlateBase {

    @Override
    protected SegmentStackIntoObjectsPooled<?> createSegmenter() {
        SegmentObjectsFromONNXModel segment = new SegmentObjectsFromONNXModel();
        segment.setDecode(new DecodeText());
        segment.setModelPath("east_text_detection.onnx");
        segment.setScaleInput(createScaleInput());
        segment.setSubtractMean(new DoubleList(123.68, 116.78, 103.94));
        segment.setInputName("input_images:0");
        segment.setIncludeBatchDimension(true);
        segment.setInterleaveChannels(true);
        return new SuppressNonMaxima<>(segment, new ConditionallyMergeOverlappingObjects(), false);
    }

    private ScaleCalculator createScaleInput() {
        LargestMultipleWithin largestMultiple = new LargestMultipleWithin();
        largestMultiple.setMinimumSize(new SizeXY(32, 32));
        largestMultiple.setMaxScaleFactor(23);
        return largestMultiple;
    }
}
