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

package ch.ethz.biol.cell.imageprocessing.stack.color;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.overlay.bean.DrawObject;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.io.generator.raster.obj.rgb.DrawObjectsGenerator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.generator.ColorSetGenerator;
import org.anchoranalysis.io.bean.color.generator.HSBColorSetGenerator;
import org.anchoranalysis.io.bean.color.generator.ShuffleColorSetGenerator;
import org.anchoranalysis.io.bean.object.writer.Filled;
import org.anchoranalysis.io.bean.object.writer.Outline;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import io.vavr.control.Either;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ColoredObjectsStackCreator {

    public static final ColorSetGenerator DEFAULT_COLOR_SET_GENERATOR =
            new ShuffleColorSetGenerator(new HSBColorSetGenerator());

    /**
     * @param objects
     * @param outline
     * @param outlineWidth
     * @param force2D
     * @param background
     * @param colors list of colors. If null, it is automatically generated.
     * @return
     * @throws CreateException
     */
    public static Stack create(
            ObjectCollection objects,
            boolean outline,
            int outlineWidth,
            boolean force2D,
            DisplayStack background,
            ColorList colors)
            throws CreateException {

        try {

            DrawObject drawObject = outline ? new Outline(outlineWidth, force2D) : new Filled();

            if (colors == null) {
                colors = DEFAULT_COLOR_SET_GENERATOR.generateColors(objects.size());
            }

            DrawObjectsGenerator generator =
                    new DrawObjectsGenerator(
                            drawObject,
                            new ObjectCollectionWithProperties(objects),
                            Either.right(background),
                            colors);

            return generator.generate();

        } catch (OutputWriteFailedException | OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
