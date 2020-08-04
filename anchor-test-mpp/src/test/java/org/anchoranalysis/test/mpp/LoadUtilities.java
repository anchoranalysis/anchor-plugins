/*-
 * #%L
 * anchor-test-mpp
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

package org.anchoranalysis.test.mpp;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;

public class LoadUtilities {

    public static ObjectMask openLargestObjectBinaryFrom(
            String suffix, TestLoaderImageIO testLoader) throws CreateException {
        Stack stack = testLoader.openStackFromTestPath(path(suffix));
        return largestObjFromStack(stack);
    }

    /**
     * Gets largest connected component from a binary-image
     *
     * @throws CreateException
     */
    private static ObjectMask largestObjFromStack(Stack stack) throws CreateException {

        CreateFromConnectedComponentsFactory cc = new CreateFromConnectedComponentsFactory();
        ObjectCollection objects =
                cc.createConnectedComponents(
                        new Mask(stack.getChannel(0)));

        return findLargestObj(objects);
    }

    private static ObjectMask findLargestObj(ObjectCollection objects) {
        int maxSize = -1;
        ObjectMask maxObj = null;
        for (ObjectMask objectMask : objects) {
            int size = objectMask.numberVoxelsOn();
            if (size > maxSize) {
                maxSize = size;
                maxObj = objectMask;
            }
        }
        return maxObj;
    }

    private static String path(String suffix) {
        return String.format("binaryImageObj/obj%s.tif", suffix);
    }
}
