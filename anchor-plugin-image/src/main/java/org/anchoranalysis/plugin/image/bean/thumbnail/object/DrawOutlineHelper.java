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
package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import io.vavr.control.Either;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.io.generator.raster.object.rgb.DrawObjectsGenerator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.RGBColorBean;
import org.anchoranalysis.io.bean.object.writer.Outline;
import org.anchoranalysis.io.color.ColorIndexModulo;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

@AllArgsConstructor
class DrawOutlineHelper {

    private final RGBColorBean color;
    private final int outlineWidth;

    /**
     * Draws the outline of all objects on the background in the color of {@code
     * colorUnselectedObjects}
     *
     * @param backgroundScaled the scaled background
     * @param objectsUnscaled the unscaled objects to draw
     * @return the background-stack with the outline of all objects drawn on it
     * @throws OperationFailedException
     */
    public Stack drawObjects(Stack backgroundScaled, ObjectCollection objects)
            throws OperationFailedException {
        try {
            DisplayStack displayStack = DisplayStack.create(backgroundScaled);

            DrawObjectsGenerator drawOthers =
                    new DrawObjectsGenerator(
                            new Outline(outlineWidth),
                            new ObjectCollectionWithProperties(objects),
                            Either.right(displayStack),
                            colorsForUnselected());
            return drawOthers.generate();
        } catch (OutputWriteFailedException | CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private ColorIndex colorsForUnselected() {
        return new ColorIndexModulo(new ColorList(color.rgbColor()));
    }
}
