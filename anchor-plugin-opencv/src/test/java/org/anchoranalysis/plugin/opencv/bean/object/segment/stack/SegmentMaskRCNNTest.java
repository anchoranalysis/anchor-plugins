package org.anchoranalysis.plugin.opencv.bean.object.segment.stack;

import java.util.Arrays;
import java.util.List;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.RemoveOverlappingObjects;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.plugin.opencv.test.ImageLoader;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.BoundingBoxFactory;

/**
 * Tests {@link SegmentMaskRCNN}.
 *
 * @author Owen Feehan
 */
class SegmentMaskRCNNTest extends SegmentStackTestBase {

    private static final BoundingBox BOX = BoundingBoxFactory.at(116, 18, 576, 398);

    private ImageLoader loader = new ImageLoader();

    @Override
    protected SegmentStackIntoObjectsPooled<?> createSegmenter() {
        return new SuppressNonMaxima<>(
                new SegmentMaskRCNN(), new RemoveOverlappingObjects(), false);
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
