package org.anchoranalysis.plugin.onnx.bean.object.segment.decode.instance;

import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.bean.primitive.DoubleList;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.exception.friendly.AnchorFriendlyRuntimeException;
import org.anchoranalysis.image.bean.interpolator.Interpolator;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.inference.bean.segment.instance.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.image.inference.bean.segment.instance.SuppressNonMaximum;
import org.anchoranalysis.io.imagej.bean.interpolator.ImageJ;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.ThresholdConfidence;
import org.anchoranalysis.plugin.image.bean.scale.FitTo;
import org.anchoranalysis.plugin.onnx.bean.object.segment.decode.instance.text.DecodeEAST;
import org.anchoranalysis.plugin.onnx.bean.object.segment.stack.SegmentObjectsFromONNXModel;
import org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance.InstanceSegmentationTestBase;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.BoundingBoxFactory;

/**
 * Tests {@link SegmentObjectsFromONNXModel} together with {@link DecodeEAST}.
 *
 * @author Owen Feehan
 */
class SegmentTextFromONNXTest extends InstanceSegmentationTestBase {

    @Override
    protected BoundingBox targetBox() {
        return BoundingBoxFactory.at(307, 310, 208, 46);
    }

    @Override
    protected SegmentStackIntoObjectsPooled<?> createSegmenter() {
        SegmentObjectsFromONNXModel segment = new SegmentObjectsFromONNXModel();
        segment.setDecode(new DecodeEAST());
        segment.setModelPath("east_text_detection.onnx");
        segment.setScaleInput(createScaleInput());
        segment.setSubtractMean(new DoubleList(123.68, 116.78, 103.94));
        segment.setInputName("input_images:0");
        segment.setIncludeBatchDimension(true);
        segment.setInterleaveChannels(true);
        segment.setReadFromResources(true);
        
        try {
            RegisterBeanFactories.getDefaultInstances().putInstanceFor(Interpolator.class, new ImageJ());
            segment.checkMisconfigured(RegisterBeanFactories.getDefaultInstances());
        } catch (BeanMisconfiguredException e) {
            throw new AnchorFriendlyRuntimeException(e);
        }
        
        return new SuppressNonMaximum<>(segment, new ThresholdConfidence(), false);
    }

    private ScaleCalculator createScaleInput() {
        FitTo largestMultiple = new FitTo();
        largestMultiple.setTargetSize(new SizeXY(1280, 720));
        largestMultiple.setMultipleOf(32);
        return largestMultiple;
    }
}
