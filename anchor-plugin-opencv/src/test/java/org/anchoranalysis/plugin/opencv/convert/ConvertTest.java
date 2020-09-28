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
package org.anchoranalysis.plugin.opencv.convert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.plugin.opencv.test.ImageLoader;
import org.junit.Test;
import org.opencv.core.Mat;

/**
 * Tests conversions to and from {@link Mat}.
 *
 * @author Owen Feehan
 */
public class ConvertTest {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

    private ImageLoader loader = new ImageLoader();

    @Test
    public void testGrayScale8Bit() throws OperationFailedException {
        testConversion(loader.carGrayscale8Bit(), true);
    }

    @Test
    public void testGrayScale16Bit() throws OperationFailedException {
        testConversion(loader.carGrayscale16Bit(), true);
    }

    @Test
    public void testRGB() throws OperationFailedException {
        testConversion(loader.carRGB(), false);
    }

    private void testConversion(Stack stack, boolean expectFinalStackDirect)
            throws OperationFailedException {
        try {
            CVInit.blockUntilLoaded();

            // Convert to Mat
            Mat mat = ConvertToMat.fromStack(stack);

            // Convert from Mat back to stack
            Stack stackCopiedBack = ConvertFromMat.toStack(mat);

            // Image-resolution is permitted to be different
            assertTrue("voxel by voxel equals", stack.equalsDeep(stackCopiedBack, false));

            // We check if the buffer is backed by an array, as a proxy for whether it was created
            //   internally by Anchor or whether it came from JavaCPP's OpenCV wrapper
            // This gives an indication if we are reusing buffers as opposed to creating new memory
            // and copying.
            assertEquals(
                    "buffer backed by array",
                    expectFinalStackDirect,
                    isBufferDirect(stackCopiedBack));

        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private static boolean isBufferDirect(Stack stack) {
        return stack.getChannel(0).voxels().any().slice(0).isDirect();
    }
}
