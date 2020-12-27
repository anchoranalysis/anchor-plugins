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

package org.anchoranalysis.plugin.opencv.bean.object.provider.text;

import java.nio.file.Path;
import java.util.List;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.ImageInitParamsFactory;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.ConditionallyMergeOverlappingObjects;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.opencv.bean.object.segment.stack.SegmentText;
import org.anchoranalysis.plugin.opencv.bean.object.segment.stack.SuppressNonMaxima;
import org.anchoranalysis.plugin.opencv.test.ImageLoader;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.test.image.InputOutputContextFixture;
import org.anchoranalysis.test.image.WriteIntoDirectory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link SegmentText}.
 *
 * <p>Note that the weights file for the EAST model is duplicated in src/test/resources, as well as
 * its usual location in the models/ directory of the Anchor distribution. This is as it's difficult
 * to determine a path to the models/ directory at test-time
 *
 * @author Owen Feehan
 */
class SegmentTextTest {

    private ImageLoader loader = new ImageLoader();

    private SegmentStackIntoObjectsPooled<?> segmenter;

    @TempDir Path temporaryDirectory;
    
    private WriteIntoDirectory writer = new WriteIntoDirectory(temporaryDirectory, false);

    @BeforeEach
    void setUp() throws InitException {
        segmenter =
                new SuppressNonMaxima<>(
                        new SegmentText(), new ConditionallyMergeOverlappingObjects());
        initSegmenter();
    }

    @Test
    void testRGB() throws SegmentationFailedException {
        assertExpectedSegmentation(loader.carRGB(), SegmentTextResults.rgb());
    }

    @Test
    void testGrayscale8Bit() throws SegmentationFailedException {
        assertExpectedSegmentation(loader.carGrayscale8Bit(), SegmentTextResults.grayscale());
    }

    private static int count = 0;

    private void assertExpectedSegmentation(Stack stack, List<BoundingBox> expectedBoxes)
            throws SegmentationFailedException {
        SegmentedObjects segmentResults = segmenter.segment(stack);
        writer.writeObjects("objects" + count++, segmentResults.asObjects(), loader.carRGB());
        ExpectedBoxesChecker.assertExpectedBoxes(segmentResults.asObjects(), expectedBoxes);
    }

    private void initSegmenter() throws InitException {
        InputOutputContext context =
                InputOutputContextFixture.withSuppressedLogger(loader.modelDirectory());
        segmenter.initRecursive(ImageInitParamsFactory.create(context), context.getLogger());
    }
}
