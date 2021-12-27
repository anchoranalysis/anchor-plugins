package org.anchoranalysis.plugin.onnx.bean.object.segment.decode.instance;

import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.exception.friendly.AnchorFriendlyRuntimeException;
import org.anchoranalysis.image.bean.interpolator.Interpolator;
import org.anchoranalysis.image.inference.bean.reduce.RemoveOverlappingObjects;
import org.anchoranalysis.image.inference.bean.segment.instance.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.image.inference.bean.segment.instance.SuppressNonMaximum;
import org.anchoranalysis.io.imagej.bean.interpolator.ImageJ;
import org.anchoranalysis.plugin.image.bean.scale.ToDimensions;
import org.anchoranalysis.plugin.onnx.bean.object.segment.decode.instance.maskrcnn.DecodeMaskRCNN;
import org.anchoranalysis.plugin.onnx.bean.object.segment.stack.SegmentObjectsFromONNXModel;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.BoundingBoxFactory;
import org.anchoranalysis.test.image.segment.InstanceSegmentationTestBase;

/**
 * Tests {@link SegmentObjectsFromONNXModel} together with {@link DecodeMaskRCNN}.
 *
 * @author Owen Feehan
 */
class SegmentRCNNFromONNXTest extends InstanceSegmentationTestBase {

    @Override
    protected BoundingBox targetBox() {
        return BoundingBoxFactory.at(116, 18, 576, 398);
    }

    @Override
    protected SegmentStackIntoObjectsPooled<?> createSegmenter() {
        SegmentObjectsFromONNXModel segment = new SegmentObjectsFromONNXModel();
        segment.setDecode(new DecodeMaskRCNN());
        segment.setModelPath("MaskRCNN-10.onnx");
        segment.setClassLabelsPath("mscoco_labels.names");
        segment.setScaleInput(new ToDimensions(1088, 800));
        segment.setInputName("image");
        segment.setReadFromResources(true);
        
        try {
            RegisterBeanFactories.getDefaultInstances().putInstanceFor(Interpolator.class, new ImageJ());
            segment.checkMisconfigured(RegisterBeanFactories.getDefaultInstances());
        } catch (BeanMisconfiguredException e) {
            throw new AnchorFriendlyRuntimeException(e);
        }
        
        return new SuppressNonMaximum<>(segment, new RemoveOverlappingObjects(), false);
    }
}
