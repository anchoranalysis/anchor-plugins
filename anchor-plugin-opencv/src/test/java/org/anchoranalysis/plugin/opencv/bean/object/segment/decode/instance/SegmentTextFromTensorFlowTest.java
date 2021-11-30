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

package org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance;

import org.anchoranalysis.bean.primitive.DoubleList;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.inference.bean.segment.instance.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.image.inference.bean.segment.instance.SuppressNonMaxima;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.ConditionallyMergeOverlappingObjects;
import org.anchoranalysis.plugin.image.bean.scale.LargestMultipleWithin;
import org.anchoranalysis.plugin.opencv.bean.object.segment.stack.SegmentObjectsFromTensorFlowModel;

/**
 * Tests {@link SegmentObjectsFromTensorFlowModel} together with {@link DecodeText}.
 *
 * <p>Note that the weights file for the EAST model is duplicated in src/test/resources, as well as
 * its usual location in the models/ directory of the Anchor distribution. This is as it's difficult
 * to determine a path to the models/ directory at test-time
 *
 * @author Owen Feehan
 */
class SegmentTextFromTensorFlowTest extends SegmentNumberPlateBase {

    @Override
    protected SegmentStackIntoObjectsPooled<?> createSegmenter() {
        SegmentObjectsFromTensorFlowModel segment = new SegmentObjectsFromTensorFlowModel();
        segment.setDecode(new DecodeText());
        segment.setModelBinaryPath("frozen_east_text_detection.pb");
        segment.setScaleInput(createScaleInput());
        segment.setSubtractMean(new DoubleList(123.68, 116.78, 103.94));
        return new SuppressNonMaxima<>(segment, new ConditionallyMergeOverlappingObjects(), false);
    }

    private ScaleCalculator createScaleInput() {
        LargestMultipleWithin largestMultiple = new LargestMultipleWithin();
        largestMultiple.setMinimumSize(new SizeXY(32, 32));
        largestMultiple.setMaxScaleFactor(23);
        return largestMultiple;
    }
}
