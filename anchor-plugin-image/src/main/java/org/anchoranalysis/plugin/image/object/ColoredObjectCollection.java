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
package org.anchoranalysis.plugin.image.object;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.image.voxel.object.factory.ObjectCollectionFactory;

/**
 * Associates a {@link ColorList} with a {@link ObjectCollection}
 *
 * <p>Operations ensure both maintain an identical number of objects
 *
 * @author Owen Feehan
 */
public class ColoredObjectCollection {

    /**
     * The objects in the collection. This will always have the same number of items as {@code
     * colors}.
     */
    @Getter private final ObjectCollection objects;

    /**
     * The colors in the collection. This will always have the same number of items as {@code
     * objects}.
     */
    @Getter private final ColorList colors;

    /** Create with an empty collection. */
    public ColoredObjectCollection() {
        this.objects = new ObjectCollection();
        this.colors = new ColorList();
    }

    /**
     * Create with a single object and color.
     *
     * @param object the object
     * @param color the color
     */
    public ColoredObjectCollection(ObjectMask object, RGBColor color) {
        this.objects = ObjectCollectionFactory.of(object);
        this.colors = new ColorList(color);
    }

    /**
     * Create with an existing object and color list.
     *
     * <p>Both arguments are reused internally as data-structures.
     *
     * @param objects the object
     * @param colors the colors, which must have the same number of items as {@code objects}
     */
    public ColoredObjectCollection(ObjectCollection objects, ColorList colors) {
        Preconditions.checkArgument(objects.size() == colors.size());
        this.objects = objects;
        this.colors = colors;
    }

    /**
     * Adds objects from a {@link ObjectCollectionProvider} all with one specific color
     *
     * @param provider provides the objects
     * @param color the color
     * @throws OperationFailedException if the provider cannot create the objects
     */
    public void addObjectsWithColor(ObjectCollectionProvider provider, RGBColor color)
            throws OperationFailedException {

        try {
            // If objects were created, add some corresponding colors
            OptionalFactory.create(provider)
                    .ifPresent(
                            objectsCreated -> {
                                objects.addAll(objectsCreated);
                                colors.addMultiple(color, objectsCreated.size());
                            });
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
