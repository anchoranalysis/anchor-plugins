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

package org.anchoranalysis.plugin.opencv.bean.object.segment.stack;

import java.util.Arrays;
import java.util.List;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.ConditionallyMergeOverlappingObjects;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.plugin.opencv.test.ImageLoader;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.BoundingBoxFactory;

/**
 * Tests {@link SegmentText}.
 *
 * <p>Note that the weights file for the EAST model is duplicated in src/test/resources, as well as
 * its usual location in the models/ directory of the Anchor distribution. This is as it's difficult
 * to determine a path to the models/ directory at test-time
 *
 * @author Owen Feehan
 */
class SegmentTextTest extends SegmentStackTestBase {

    private ImageLoader loader = new ImageLoader();

    private static final BoundingBox BOX3 = BoundingBoxFactory.at(438, 310, 78, 40);

    @Override
    protected SegmentStackIntoObjectsPooled<?> createSegmenter() {
        return new SuppressNonMaxima<>(
                new SegmentText(), new ConditionallyMergeOverlappingObjects(), false);
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
        BoundingBox box1 = BoundingBoxFactory.at(302, 315, 127, 42);
        BoundingBox box2 = BoundingBoxFactory.at(393, 199, 31, 28);
        return Arrays.asList(box1, box2, BOX3);
    }

    @Override
    protected List<BoundingBox> expectedBoxesGrayscale() {
        BoundingBox box1 = BoundingBoxFactory.at(316, 319, 104, 33);
        BoundingBox box2 = BoundingBoxFactory.at(394, 199, 27, 27);
        return Arrays.asList(box1, box2);
    }
}
