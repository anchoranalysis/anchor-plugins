/*-
 * #%L
 * anchor-plugin-onnx
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.onnx.bean.object.segment.decode.instance;

import org.anchoranalysis.bean.primitive.DoubleList;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.inference.bean.segment.instance.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.image.inference.bean.segment.instance.SuppressNonMaximum;
import org.anchoranalysis.io.imagej.bean.interpolator.ImageJ;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.ThresholdConfidence;
import org.anchoranalysis.plugin.image.bean.scale.FitTo;
import org.anchoranalysis.plugin.onnx.bean.object.segment.decode.instance.text.DecodeEAST;
import org.anchoranalysis.plugin.onnx.bean.object.segment.stack.SegmentObjectsFromONNXModel;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.BoundingBoxFactory;
import org.anchoranalysis.test.image.io.BeanInstanceMapFixture;
import org.anchoranalysis.test.image.segment.InstanceSegmentationTestBase;

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

        BeanInstanceMapFixture.ensureInterpolator(new ImageJ());
        BeanInstanceMapFixture.check(segment);

        return new SuppressNonMaximum<>(segment, new ThresholdConfidence(), false);
    }

    private ScaleCalculator createScaleInput() {
        FitTo largestMultiple = new FitTo();
        largestMultiple.setTargetSize(new SizeXY(1280, 720));
        largestMultiple.setMultipleOf(32);
        return largestMultiple;
    }
}
