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
package org.anchoranalysis.plugin.opencv.bean.stack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImage;
import org.junit.jupiter.api.Test;

class OpenCVReaderTest {

    private TestLoaderImage loader =
            new TestLoaderImage(TestLoader.createFromMavenWorkingDirectory(), new OpenCVReader());

    private static final Extent EXPECTED_JAPAN_EXTENT = new Extent(3888, 5184, 1);

    @Test
    void testJpegRGBNormalOrientation() {
        loadAndAssert("stackReader/japan_correct_orientation.jpg", EXPECTED_JAPAN_EXTENT);
    }

    @Test
    void testJpegRGBAlternativeOrientation() {
        loadAndAssert("stackReader/japan_exif_alternative_orientation.jpg", EXPECTED_JAPAN_EXTENT);
    }

    private void loadAndAssert(String imageTestPath, Extent extent) {
        Stack stack = loader.openStackFromTestPath(imageTestPath);
        assertEquals(extent, stack.extent(), "expected extent");
        assertTrue(stack.allChannelsHaveIdenticalType(), "all channels identical type");
        assertEquals(
                UnsignedByteVoxelType.INSTANCE,
                stack.getChannel(0).getVoxelDataType(), "channel type");
        assertTrue(stack.isRGB(), "rgb");
    }
}
