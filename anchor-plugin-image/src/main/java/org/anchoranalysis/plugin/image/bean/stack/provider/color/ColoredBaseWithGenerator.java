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
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.color.scheme.ColorScheme;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.plugin.image.object.ColoredObjectCollection;

/**
 * Like {@link ColoredBase} but uses a generator to determine the colors for the objects.
 *
 * @author Owen Feehan
 */
public abstract class ColoredBaseWithGenerator extends ColoredBase {

    // START BEAN PROPERTIES
    /** Colors to use for drawing objects. */
    @BeanField @Getter @Setter private ColorScheme colors = DEFAULT_COLOR_SET_GENERATOR;
    // END BEAN PROPERTIES

    @Override
    protected ColoredObjectCollection coloredObjectsToDraw(Dimensions backgroundDimensions)
            throws CreateException {
        return addColors(objectsToDraw(backgroundDimensions));
    }

    /**
     * The objects to draw (without any colors) on the background.
     *
     * @param backgroundDimensions the dimensions of the background.
     * @return the {@link ObjectCollection} to be drawn on the background.
     * @throws CreateException if the objects cannot be created.
     */
    protected abstract ObjectCollection objectsToDraw(Dimensions backgroundDimensions)
            throws CreateException;

    /**
     * Adds colors to the created objects.
     *
     * @param objectsCreated the {@link ObjectCollection} to add colors to.
     * @return a {@link ColoredObjectCollection} with colors added to the objects.
     * @throws CreateException if colors cannot be added to the objects.
     */
    private ColoredObjectCollection addColors(ObjectCollection objectsCreated)
            throws CreateException {
        try {
            ColorList colorsGenerated = colors.createList(objectsCreated.size());
            return new ColoredObjectCollection(objectsCreated, colorsGenerated);
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
