package org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance;

import java.util.Arrays;
import java.util.List;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.RemoveOverlappingObjects;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.plugin.image.bean.scale.ConstantScaleFactor;
import org.anchoranalysis.plugin.opencv.bean.object.segment.stack.SegmentObjectsFromTensorFlowModel;
import org.anchoranalysis.plugin.opencv.bean.object.segment.stack.SuppressNonMaxima;
import org.anchoranalysis.plugin.opencv.test.ImageLoader;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.BoundingBoxFactory;

/**
 * Tests {@link DecodeMaskRCNN}.
 *
 * @author Owen Feehan
 */
class DecodeMaskRCNNTest extends DecodeInstanceSegmentationTestBase {

    private static final BoundingBox BOX = BoundingBoxFactory.at(116, 18, 576, 398);

    private ImageLoader loader = new ImageLoader();

    @Override
    protected SegmentStackIntoObjectsPooled<?> createSegmenter() {
        SegmentObjectsFromTensorFlowModel segment = new SegmentObjectsFromTensorFlowModel();
        segment.setDecode( new DecodeMaskRCNN() );
        segment.setModelBinaryPath("frozen_mask_rcnn_inception_v2_coco_2018_01_28.pb");
        segment.setModelTextGraphPath("mask_rcnn_inception_v2_coco_2018_01_28.pbtxt");
        segment.setClassLabelsPath("mscoco_labels.names");
        segment.setScaleInput( new ConstantScaleFactor() );
        return new SuppressNonMaxima<>(
                segment, new RemoveOverlappingObjects(), false);
    }

    @Override
    protected Stack stackRGB() {
        return loader.carRGB();
    }

    @Override
    protected Stack stackGrayscale() {
        return loader.carGrayscale8Bit();
    }

    @Override
    protected List<BoundingBox> expectedBoxesRGB() {
        return Arrays.asList(BOX);
    }

    @Override
    protected List<BoundingBox> expectedBoxesGrayscale() {
        return Arrays.asList(BOX);
    }
}
