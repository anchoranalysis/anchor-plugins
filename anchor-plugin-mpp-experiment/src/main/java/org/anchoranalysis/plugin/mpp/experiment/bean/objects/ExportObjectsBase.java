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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.overlay.bean.DrawObject;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.generator.raster.bbox.ExtractedBoundingBoxGenerator;
import org.anchoranalysis.image.io.generator.raster.obj.rgb.DrawCroppedObjectsGenerator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

public abstract class ExportObjectsBase<T extends InputFromManager, S> extends Task<T, S> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;

    /**
     * Padding placed on each side of the outputted image (if it's within the image) in XY
     * directions
     */
    @BeanField @Getter @Setter private int paddingXY = 0;

    /**
     * Padding placed on each side of the outputted image (if it's within the image) in Z direction
     */
    @BeanField @Getter @Setter private int paddingZ = 0;
    // END BEAN PROPERTIES

    protected ObjectCollection inputObjects(ImageInitParams so, Logger logger)
            throws CreateException, InitException {
        ObjectCollectionProvider objectsDuplicated = objects.duplicateBean();
        objectsDuplicated.initRecursive(so, logger);
        return objectsDuplicated.create();
    }

    /**
     * Adds padding (if set) to an object-mask
     *
     * @param object object-mask to be padded
     * @param dimensions size of image
     * @return either the exist object-mask (if no padding is to be added) or a padded object-mask
     * @throws OutputWriteFailedException
     */
    protected ObjectMask maybePadObject(ObjectMask object, ImageDimensions dimensions) {

        if (paddingXY == 0 && paddingZ == 0) {
            return object;
        }

        BoundingBox bboxToExtract =
                object.getBoundingBox()
                        .growBy(
                                new Point3i(paddingXY, paddingXY, paddingZ),
                                dimensions.getExtent());

        return BoundingBoxUtilities.createObjectForBoundingBox(object, bboxToExtract);
    }

    protected ExtractedBoundingBoxGenerator createBoundingBoxGeneratorForStack(
            Stack stack, String manifestFunction) {
        ExtractedBoundingBoxGenerator generator =
                new ExtractedBoundingBoxGenerator(stack, manifestFunction);
        generator.setPaddingXY(paddingXY);
        generator.setPaddingZ(paddingZ);
        return generator;
    }

    protected DrawCroppedObjectsGenerator createRGBMaskGenerator(
            DrawObject drawObject, DisplayStack background, ColorList colorList) {
        DrawCroppedObjectsGenerator delegate =
                new DrawCroppedObjectsGenerator(drawObject, background, colorList);
        delegate.setPaddingXY(paddingXY);
        delegate.setPaddingZ(paddingZ);
        return delegate;
    }
}
