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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.anchoranalysis.bean.OptionalProviderFactory;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.core.object.properties.ObjectWithProperties;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/**
 * Associates a {@link ColorList} with a {@link ObjectCollection}
 *
 * <p>Operations ensure both maintain an identical number of objects
 *
 * @author Owen Feehan
 */
public class ColoredObjectCollection {

    /** Each object-mask and corresponding color in the collection. */
    private final List<ColoredObject> list;

    /** Create with an empty collection. */
    public ColoredObjectCollection() {
        this.list = new LinkedList<>();
    }

    /**
     * Create from a stream of {@link ColoredObject}s.
     *
     * @param stream the stream.
     */
    public ColoredObjectCollection(Stream<ColoredObject> stream) {
        this.list = stream.collect(Collectors.toList());
    }

    /**
     * Create with a single object and color.
     *
     * @param object the object
     * @param color the color
     */
    public ColoredObjectCollection(ObjectMask object, RGBColor color) {
        this.list = Arrays.asList(new ColoredObject(object, color));
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
        this.list = FunctionalList.zip(objects.asList(), colors.asList(), ColoredObject::new);
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
            Optional<ObjectCollection> objects = OptionalProviderFactory.create(provider);
            if (objects.isPresent()) {
                addObjectsToList(objects.get(), color);
            }
        } catch (ProvisionFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * A maximum-intensity projection.
     *
     * <p>This flattens across z-dimension, setting a voxel to <i>on</i> if it is <i>on</i> in any
     * one slice.
     *
     * <p>This is an <b>immutable</b> operation.
     *
     * @return a new {@link ColoredObject} flattened in Z dimension.
     */
    public ColoredObjectCollection flattenZ() {
        return new ColoredObjectCollection(list.stream().map(ColoredObject::flattenZ));
    }

    /**
     * Derives a {@link ColorList} from the collection.
     *
     * @return a newly created list of colors, in identical order to the collection.
     */
    public ColorList deriveColorList() {
        return new ColorList(list.stream().map(colored -> colored.getColor()));
    }

    /**
     * Creates a stream of {@link ObjectWithProperties} derived from the collection.
     *
     * @return a newly created stream, in identical order to the collection.
     */
    public Stream<ObjectWithProperties> streamObjectWithProperties() {
        return list.stream().map(colored -> new ObjectWithProperties(colored.getObject()));
    }

    private void addObjectsToList(ObjectCollection objects, RGBColor color) {
        objects.asList().forEach(object -> list.add(new ColoredObject(object, color)));
    }
}
