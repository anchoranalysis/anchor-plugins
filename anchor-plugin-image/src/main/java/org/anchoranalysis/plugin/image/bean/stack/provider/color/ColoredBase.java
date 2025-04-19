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
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.bean.shared.color.scheme.ColorScheme;
import org.anchoranalysis.bean.shared.color.scheme.HSB;
import org.anchoranalysis.bean.shared.color.scheme.Shuffle;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.bean.displayer.StackDisplayer;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.core.stack.ProviderAsStack;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.bean.object.draw.Filled;
import org.anchoranalysis.image.io.bean.object.draw.Outline;
import org.anchoranalysis.image.io.object.output.rgb.DrawObjectsGenerator;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.overlay.bean.DrawObject;
import org.anchoranalysis.plugin.image.object.ColoredObjectCollection;

/**
 * Base class for providers that draw entities (an outline or filled) on a background.
 *
 * @author Owen Feehan
 */
public abstract class ColoredBase extends StackProvider {

    /** The default {@link ColorScheme} used for generating colors. */
    protected static final ColorScheme DEFAULT_COLOR_SET_GENERATOR = new Shuffle(new HSB());

    // START BEAN PROPERTIES
    /**
     * If true, objects and the background are flattened in the z dimension (via maximum intensity
     * projection), so that a 2D image is produced.
     */
    @BeanField @Getter @Setter private boolean flatten = false;

    /** If true, an outline is drawn around the entries. If false, a filled-in shape is drawn. */
    @BeanField @Getter @Setter private boolean outline = true;

    /** The width of the outline (only relevant if {@code outline==true}). */
    @BeanField @Getter @Setter @Positive private int outlineWidth = 1;

    /**
     * If true, the outline is suppressed in the z-dimension i.e. a boundary only in z-dimension is
     * not outlined (only relevant if {@code outline==true}).
     */
    @BeanField @Getter @Setter private boolean suppressOutlineZ = true;

    /** The background. Either {@code stackBackground} or this should be defined but not both. */
    @BeanField @Getter @Setter private ProviderAsStack background;

    /** How to convert an image to be displayed to the user. */
    @BeanField @Getter @Setter @DefaultInstance private StackDisplayer displayer;
    // END BEAN PROPERTIES

    @Override
    public Stack get() throws ProvisionFailedException {
        try {
            DisplayStack unflattened = createUnflattenedBackground();
            return drawOnBackground(coloredObjectsToDraw(unflattened.dimensions()), unflattened);
        } catch (CreateException e) {
            throw new ProvisionFailedException(e);
        }
    }

    /**
     * Creates colored-objects to be drawn.
     *
     * @param backgroundDimensions dimensions of the background on which objects are drawn.
     * @return a {@link ColoredObjectCollection} describing the objects to be drawn.
     * @throws CreateException if the colored objects cannot be created.
     */
    protected abstract ColoredObjectCollection coloredObjectsToDraw(Dimensions backgroundDimensions)
            throws CreateException;

    /**
     * Draws objects on the background.
     *
     * @param objects the {@link ColoredObjectCollection} to draw.
     * @param background the background {@link DisplayStack} to draw on.
     * @return the resulting {@link Stack} after drawing.
     * @throws CreateException if the drawing operation fails.
     */
    private Stack drawOnBackground(ColoredObjectCollection objects, DisplayStack background)
            throws CreateException {
        return drawOnBackgroundAfterFlattening(maybeFlatten(objects), maybeFlatten(background));
    }

    /**
     * Creates a {@link DrawObject} based on the current configuration.
     *
     * @return a new {@link DrawObject} instance.
     */
    private DrawObject createDrawer() {
        if (outline) {
            return new Outline(getOutlineWidth(), !flatten && !suppressOutlineZ);
        } else {
            return new Filled();
        }
    }

    /**
     * Creates the unflattened background {@link DisplayStack}.
     *
     * @return the unflattened background as a {@link DisplayStack}.
     * @throws CreateException if the background cannot be created.
     */
    private DisplayStack createUnflattenedBackground() throws CreateException {
        try {
            return displayer.deriveFrom(background.getAsStack());
        } catch (ProvisionFailedException e) {
            throw new CreateException(e);
        }
    }

    /**
     * Draws objects on the background after flattening (if necessary).
     *
     * @param objects the {@link ColoredObjectCollection} to draw.
     * @param background the background {@link DisplayStack} to draw on.
     * @return the resulting {@link Stack} after drawing.
     * @throws CreateException if the drawing operation fails.
     */
    private Stack drawOnBackgroundAfterFlattening(
            ColoredObjectCollection objects, DisplayStack background) throws CreateException {

        // TODO come up with a more efficient way that feed in a separate list of objects and colors
        try {
            DrawObjectsGenerator generator =
                    DrawObjectsGenerator.withBackgroundAndColors(
                            createDrawer(), background, objects.deriveColorList());
            ObjectCollectionWithProperties objectsToTransform =
                    new ObjectCollectionWithProperties(objects.streamObjectWithProperties());
            return generator.transform(objectsToTransform);

        } catch (OutputWriteFailedException e) {
            throw new CreateException(e);
        }
    }

    /**
     * Flattens the {@link DisplayStack} if necessary.
     *
     * @param stack the {@link DisplayStack} to potentially flatten.
     * @return the flattened or original {@link DisplayStack}.
     */
    private DisplayStack maybeFlatten(DisplayStack stack) {
        if (flatten) {
            return stack.projectMax();
        } else {
            return stack;
        }
    }

    /**
     * Flattens the {@link ColoredObjectCollection} if necessary.
     *
     * @param objects the {@link ColoredObjectCollection} to potentially flatten.
     * @return the flattened or original {@link ColoredObjectCollection}.
     */
    private ColoredObjectCollection maybeFlatten(ColoredObjectCollection objects) {
        if (flatten) {
            return objects.flattenZ();
        } else {
            return objects;
        }
    }
}
