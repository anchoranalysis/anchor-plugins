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

import org.anchoranalysis.image.inference.bean.reduce.RemoveOverlappingObjects;
import org.anchoranalysis.image.inference.bean.segment.instance.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.image.inference.bean.segment.instance.SuppressNonMaximum;
import org.anchoranalysis.io.imagej.bean.interpolator.ImageJ;
import org.anchoranalysis.plugin.image.bean.scale.ToDimensions;
import org.anchoranalysis.plugin.onnx.bean.object.segment.decode.instance.maskrcnn.DecodeMaskRCNN;
import org.anchoranalysis.plugin.onnx.bean.object.segment.stack.SegmentObjectsFromONNXModel;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.BoundingBoxFactory;
import org.anchoranalysis.test.image.io.BeanInstanceMapFixture;
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

        BeanInstanceMapFixture.ensureInterpolator(new ImageJ());
        BeanInstanceMapFixture.ensureStackDisplayer();
        BeanInstanceMapFixture.check(segment);

        return new SuppressNonMaximum<>(segment, new RemoveOverlappingObjects(), false);
    }
}
