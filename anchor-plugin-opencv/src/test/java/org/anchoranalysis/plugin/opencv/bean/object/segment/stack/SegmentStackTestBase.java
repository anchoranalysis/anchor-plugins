package org.anchoranalysis.plugin.opencv.bean.object.segment.stack;

import java.nio.file.Path;
import java.util.List;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.ImageInitializationFactory;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.InputOutputContextFixture;
import org.anchoranalysis.test.image.WriteIntoDirectory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Base class for testing implementations of {@link SegmentStackIntoObjectsPooled}.
 *
 * @author Owen Feehan
 */
abstract class SegmentStackTestBase {

    /** The segmentation implementation to test. */
    private SegmentStackIntoObjectsPooled<?> segmenter;

    @TempDir Path temporaryDirectory;

    private WriteIntoDirectory writer;

    @BeforeEach
    void setup() throws InitException {
        writer = new WriteIntoDirectory(temporaryDirectory, false);
        segmenter = createSegmenter();
        initSegmenter(segmenter);
    }

    @Test
    void testRGB() throws SegmentationFailedException {
        assertExpectedSegmentation(stackRGB(), expectedBoxesRGB(), "rgb");
    }

    @Test
    void testGrayscale8Bit() throws SegmentationFailedException {
        assertExpectedSegmentation(stackGrayscale(), expectedBoxesGrayscale(), "grayscale");
    }

    /** Creates the segmentation implementation to be tested. */
    protected abstract SegmentStackIntoObjectsPooled<?> createSegmenter();

    /** The RGB stack that is tested. */
    protected abstract Stack stackRGB();

    /** The grayscale stack that is tested. */
    protected abstract Stack stackGrayscale();

    /**
     * The bounding-boxes of the objects expected from the <i>RGB</i> stack as a segmentation
     * result.
     */
    protected abstract List<BoundingBox> expectedBoxesRGB();

    /**
     * The bounding-boxes of the objects expected from the <i>grayscale</i> stack as a segmentation
     * result.
     */
    protected abstract List<BoundingBox> expectedBoxesGrayscale();

    private void assertExpectedSegmentation(
            Stack stack, List<BoundingBox> expectedBoxes, String suffix)
            throws SegmentationFailedException {
        SegmentedObjects segmentResults = segmenter.segment(stack);
        writer.writeObjects("objects_" + suffix, segmentResults.asObjects(), stackRGB());
        ExpectedBoxesChecker.assertExpectedBoxes(segmentResults.asObjects(), expectedBoxes);
    }

    private static void initSegmenter(SegmentStackIntoObjectsPooled<?> segmenter)
            throws InitException {
        Path root = TestLoader.createFromMavenWorkingDirectory().getRoot();
        InputOutputContext context = InputOutputContextFixture.withSuppressedLogger(root);
        segmenter.initRecursive(ImageInitializationFactory.create(context), context.getLogger());
    }
}
