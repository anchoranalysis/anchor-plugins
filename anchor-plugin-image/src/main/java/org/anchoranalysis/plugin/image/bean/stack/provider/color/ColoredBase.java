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
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.io.bean.object.draw.Filled;
import org.anchoranalysis.image.io.bean.object.draw.Outline;
import org.anchoranalysis.image.io.generator.raster.object.rgb.DrawObjectsGenerator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.image.provider.ProviderAsStack;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.scheme.ColorScheme;
import org.anchoranalysis.io.bean.color.scheme.HSB;
import org.anchoranalysis.io.bean.color.scheme.Shuffle;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.overlay.bean.DrawObject;
import org.anchoranalysis.plugin.image.object.ColoredObjectCollection;

/**
 * Base class for providers that draw entities (an outline or filled) on a background
 *
 * @author Owen Feehan
 */
public abstract class ColoredBase extends StackProvider {

    protected static final ColorScheme DEFAULT_COLOR_SET_GENERATOR = new Shuffle(new HSB());

    // START BEAN PROPERTIES
    /**
     * If true, objects and the background are flattened in the z dimension (via maximum intensity
     * projection), so that a 2D image is produced
     */
    @BeanField @Getter @Setter private boolean flatten = false;

    /** If true, an outline is drawn around the entries. If false, a filled-in shape is drawn */
    @BeanField @Getter @Setter private boolean outline = true;

    /** The width of the outline (only relevant if {@code outline==true} */
    @BeanField @Getter @Setter @Positive private int outlineWidth = 1;

    /**
     * if true, the outline is suppressed in the z-dimension i.e. a boundary only in z-dimension is
     * not outlined (only relevant if {@code outline==true}
     */
    @BeanField @Getter @Setter private boolean suppressOutlineZ = true;

    /** The background. Either {@code stackBackground} or this should be defined but not both */
    @BeanField @Getter @Setter private ProviderAsStack background;
    // END BEAN PROPERTIES

    @Override
    public Stack create() throws CreateException {
        DisplayStack unflattened = createUnflattenedBackground();
        return drawOnBackground(coloredObjectsToDraw(unflattened.dimensions()), unflattened);
    }

    /**
     * Creates colored-objects to be drawn
     *
     * @param backgroundDimensions dimensions of the background on which objects are drawn
     * @return a colored object collection describing the objects to be drawn
     */
    protected abstract ColoredObjectCollection coloredObjectsToDraw(Dimensions backgroundDimensions)
            throws CreateException;

    private Stack drawOnBackground(ColoredObjectCollection objects, DisplayStack background)
            throws CreateException {
        return drawOnBackgroundAfterFlattening(
                maybeFlatten(objects.getObjects()), maybeFlatten(background), objects.getColors());
    }

    private DrawObject createDrawer() {
        if (outline) {
            return new Outline(getOutlineWidth(), !flatten && !suppressOutlineZ);
        } else {
            return new Filled();
        }
    }

    private DisplayStack createUnflattenedBackground() throws CreateException {
        return DisplayStack.create(background.createAsStack());
    }

    /**
     * @param objects
     * @param background
     * @param colors list of colors. If null, it is automatically generated.
     * @return
     * @throws CreateException
     */
    private Stack drawOnBackgroundAfterFlattening(
            ObjectCollection objects, DisplayStack background, ColorList colors)
            throws CreateException {

        try {
            if (colors == null) {
                colors = DEFAULT_COLOR_SET_GENERATOR.createList(objects.size());
            }

            DrawObjectsGenerator generator =
                    DrawObjectsGenerator.withBackgroundAndColors(
                            createDrawer(),
                            new ObjectCollectionWithProperties(objects),
                            background,
                            colors);

            return generator.transform();

        } catch (OutputWriteFailedException | OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private DisplayStack maybeFlatten(DisplayStack stack) {
        if (flatten) {
            return stack.maximumIntensityProjection();
        } else {
            return stack;
        }
    }

    private ObjectCollection maybeFlatten(ObjectCollection objects) {
        if (flatten) {
            return objects.stream().map(ObjectMask::flattenZ);
        } else {
            return objects;
        }
    }
}
