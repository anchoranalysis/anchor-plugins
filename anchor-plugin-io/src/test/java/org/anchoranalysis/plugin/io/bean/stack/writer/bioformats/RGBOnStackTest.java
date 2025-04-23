/*-
 * #%L
 * anchor-plugin-io
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
package org.anchoranalysis.plugin.io.bean.stack.writer.bioformats;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.test.image.io.TestLoaderImage;
import org.anchoranalysis.test.image.stackwriter.ChannelSpecification;
import org.anchoranalysis.test.image.stackwriter.IdentifierHelper;
import org.anchoranalysis.test.image.stackwriter.SavedFiles;
import org.anchoranalysis.test.image.stackwriter.StackTester;
import org.junit.jupiter.api.Test;

/**
 * Tests that the RGB flag is set as expected after loading from several different formats.
 *
 * @author Owen Feehan
 */
class RGBOnStackTest {

    /** A 8-Bit RGB Stack. */
    private static final ChannelSpecification CHANNEL_SPECIFICATION =
            new ChannelSpecification(UnsignedByteVoxelType.INSTANCE, 3, true);

    @Test
    void testOMETiff() {
        assertForFormat(ImageFileFormat.OME_TIFF.getDefaultExtension());
    }

    @Test
    void testTiff() {
        assertForFormat(ImageFileFormat.TIFF.getDefaultExtension());
    }

    @Test
    void testPNG() {
        assertForFormat(ImageFileFormat.PNG.getDefaultExtension());
    }

    private void assertForFormat(String formatExtension) {
        assertTrue(loadStack(formatExtension).isRGB());
    }

    private Stack loadStack(String extension) {
        String identifier =
                IdentifierHelper.identiferFor(
                        CHANNEL_SPECIFICATION, false, StackTester.EXTENT_IDENTIFIER, false);

        TestLoaderImage loader = new TestLoaderImage(SavedFiles.createLoader(extension));
        return loader.openStackFromTestPath(identifier + "." + extension);
    }
}
