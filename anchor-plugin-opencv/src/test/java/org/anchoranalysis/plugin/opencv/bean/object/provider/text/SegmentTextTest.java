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

import java.util.List;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.extent.box.BoundingBox;
import org.anchoranalysis.image.io.input.ImageInitParamsFactory;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.ConditionallyMergeOverlappingObjects;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.opencv.bean.object.segment.stack.SegmentText;
import org.anchoranalysis.plugin.opencv.bean.object.segment.stack.SuppressNonMaxima;
import org.anchoranalysis.plugin.opencv.test.ImageLoader;
import org.anchoranalysis.test.image.BoundIOContextFixture;
import org.anchoranalysis.test.image.WriteIntoFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests {@link SegmentText}.
 *
 * <p>Note that the weights file for the EAST model is duplicated in src/test/resources, as well as
 * its usual location in the models/ directory of the Anchor distribution. This is as it's difficult
 * to determine a path to the models/ directory at test-time
 *
 * @author Owen Feehan
 */
public class SegmentTextTest {

    private ImageLoader loader = new ImageLoader();

    private SegmentStackIntoObjectsPooled<?> segmenter;
    
    @Rule public WriteIntoFolder writer = new WriteIntoFolder(true);

    @Before
    public void setUp() throws InitException {
        segmenter = new SuppressNonMaxima<>( new SegmentText(), new ConditionallyMergeOverlappingObjects() );
        initSegmenter();
    }

    @Test
    public void testRGB() throws SegmentationFailedException {
        segmentStack(loader.carRGB(), SegmentTextResults.rgb());
    }

    @Test
    public void testGrayscale8Bit() throws SegmentationFailedException {
        segmentStack(loader.carGrayscale8Bit(), SegmentTextResults.grayscale());
    }

    private static int cnt = 0;
    
    private void segmentStack(Stack stack, List<BoundingBox> expectedBoxes)
            throws SegmentationFailedException {
        SegmentedObjects segmentResults = segmenter.segment(stack);
        writer.writeObjects("objects" + cnt++, segmentResults.asObjects(), loader.carRGB());
        ExpectedBoxesChecker.assertExpectedBoxes(segmentResults.asObjects(), expectedBoxes);
    }

    private void initSegmenter() throws InitException {
        BoundIOContext context =
                BoundIOContextFixture.withSuppressedLogger(loader.modelDirectory());
        segmenter.initRecursive(ImageInitParamsFactory.create(context), context.getLogger());
    }
}
