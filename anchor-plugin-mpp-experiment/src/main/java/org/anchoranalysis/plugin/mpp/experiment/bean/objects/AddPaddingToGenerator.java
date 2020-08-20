/*-
 * #%L
 * anchor-plugin-mpp-experiment
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
package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.size.Padding;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.ObjectsWithBoundingBox;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.IterableGeneratorBridge;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

/**
 * Adds optional padding to objects before being passed into another generator
 *
 * <p>TODO This is quite inefficient as it changes the object-mask's voxel-buffers to use the ENTIRE
 * image each time. There's a better way to do this.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class AddPaddingToGenerator {

    public static IterableGenerator<ObjectsWithBoundingBox> addPadding(
            IterableGenerator<ObjectsWithBoundingBox> generator,
            Dimensions dimensions,
            Padding padding,
            boolean keepEntireImage) {
        // Maybe we need to change the objectMask to a padded version
        return IterableGeneratorBridge.createOneToOne(
                generator,
                objects -> {
                    if (keepEntireImage) {
                        return objects.mapObjectsToUseEntireImage(dimensions);
                    } else {
                        return maybePadObjects(objects, dimensions, padding);
                    }
                });
    }

    private static ObjectsWithBoundingBox maybePadObjects(
            ObjectsWithBoundingBox objects, Dimensions dimensions, Padding padding)
            throws OperationFailedException {
        if (objects.size() == 1) {
            return new ObjectsWithBoundingBox(maybePadObject(objects.get(0), dimensions, padding));
        } else {
            throw new OperationFailedException("Padding is only supported for single-objects");
        }
    }

    /**
     * Adds padding (if set) to an object-mask
     *
     * @param object object-mask to be padded
     * @param dimensions size of image
     * @return either the exist object-mask (if no padding is to be added) or a padded object-mask
     * @throws OutputWriteFailedException
     */
    private static ObjectMask maybePadObject(
            ObjectMask object, Dimensions dimensions, Padding padding) {

        if (padding.noPadding()) {
            return object;
        }

        BoundingBox boxToExtract =
                object.boundingBox().growBy(padding.asPoint(), dimensions.extent());

        return object.mapBoundingBoxChangeExtent(boxToExtract);
    }
}
