/* (C)2020 */
package org.anchoranalysis.test.mpp;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
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
                cc.createConnectedComponents(new Mask(stack.getChnl(0), BinaryValues.getDefault()));

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
