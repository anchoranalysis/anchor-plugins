/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.bean.stack.provider.color;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.plugin.image.object.ColoredObjectCollection;

/**
 * Colors three collections of objects in RED, GREEN, BLUE channels on top of a background.
 *
 * @author Owen Feehan
 */
public class ThreeColoredObjects extends ColoredBase {

    private static final RGBColor COLOR_RED = new RGBColor(255, 0, 0);
    private static final RGBColor COLOR_GREEN = new RGBColor(0, 255, 0);
    private static final RGBColor COLOR_BLUE = new RGBColor(0, 0, 255);

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private ObjectCollectionProvider objectsRed;

    @BeanField @OptionalBean @Getter @Setter private ObjectCollectionProvider objectsBlue;

    @BeanField @OptionalBean @Getter @Setter private ObjectCollectionProvider objectsGreen;
    // END BEAN PROPERTIES

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);

        if (objectsRed == null && objectsBlue == null && objectsGreen == null) {
            throw new BeanMisconfiguredException(
                    "Either objectsRed or objectsBlue or objectsGreen must be non-null");
        }
    }

    @Override
    protected ColoredObjectCollection coloredObjectsToDraw(Dimensions backgroundDimensions)
            throws CreateException {
        ColoredObjectCollection objects = new ColoredObjectCollection();

        try {
            objects.addObjectsWithColor(objectsRed, COLOR_RED);
            objects.addObjectsWithColor(objectsGreen, COLOR_GREEN);
            objects.addObjectsWithColor(objectsBlue, COLOR_BLUE);
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
        return objects;
    }
}
