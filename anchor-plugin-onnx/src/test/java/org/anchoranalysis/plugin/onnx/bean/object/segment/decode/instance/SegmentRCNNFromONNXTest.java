package org.anchoranalysis.plugin.onnx.bean.object.segment.decode.instance;

import org.anchoranalysis.image.inference.bean.segment.instance.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.image.inference.bean.segment.instance.SuppressNonMaxima;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.RemoveOverlappingObjects;
import org.anchoranalysis.plugin.image.bean.scale.ToDimensions;
import org.anchoranalysis.plugin.onnx.bean.object.segment.stack.SegmentObjectsFromONNXModel;
import org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance.SegmentCarBase;

/**
 * Tests {@link SegmentObjectsFromONNXModel} together with {@link DecodeMaskRCNN}.
 *
 * @author Owen Feehan
 */
class SegmentRCNNFromONNXTest extends SegmentCarBase {

    @Override
    protected SegmentStackIntoObjectsPooled<?> createSegmenter() {
        SegmentObjectsFromONNXModel segment = new SegmentObjectsFromONNXModel();
        segment.setDecode(new DecodeMaskRCNN());
        segment.setModelPath("MaskRCNN-10.onnx");
        segment.setClassLabelsPath("mscoco_labels.names");
        segment.setScaleInput(new ToDimensions(1088, 800));
        segment.setInputName("image");
        segment.setReadFromResources(true);
        return new SuppressNonMaxima<>(segment, new RemoveOverlappingObjects(), false);
    }
}
