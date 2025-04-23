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

package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalProviderFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.provider.DimensionsProvider;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.core.dimensions.UnitConverter;
import org.anchoranalysis.image.core.object.MatchedObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.plugin.image.bean.object.match.MatcherIntersectionHelper;
import org.anchoranalysis.plugin.image.bean.object.provider.WithContainerBase;

/** A base class for algorithms that merge object-masks. */
public abstract class MergeBase extends WithContainerBase {

    // START BEAN PROPERTIES
    /** Provider for image dimensions. */
    @BeanField @OptionalBean @Getter @Setter private DimensionsProvider dimensions;

    // END BEAN PROPERTIES

    /** Functional interface for merging objects. */
    @FunctionalInterface
    protected static interface MergeObjects {
        /**
         * Merges a collection of objects.
         *
         * @param objects the {@link ObjectCollection} to merge
         * @return the merged {@link ObjectCollection}
         * @throws OperationFailedException if the merge operation fails
         */
        ObjectCollection mergeObjects(ObjectCollection objects) throws OperationFailedException;
    }

    /**
     * Gets an optional {@link UnitConverter} based on the image resolution.
     *
     * @return an {@link Optional} containing a {@link UnitConverter} if resolution is available,
     *     empty otherwise
     * @throws OperationFailedException if retrieving the resolution fails
     */
    protected Optional<UnitConverter> unitConvertOptional() throws OperationFailedException {
        return resolutionOptional().map(Resolution::unitConvert);
    }

    /**
     * Gets the required {@link Resolution}.
     *
     * @return the {@link Resolution}
     * @throws OperationFailedException if the resolution is not available
     */
    protected Resolution resolutionRequired() throws OperationFailedException {
        return resolutionOptional()
                .orElseThrow(
                        () ->
                                new OperationFailedException(
                                        "This algorithm requires an image-resolution to be set via resProvider"));
    }

    /**
     * Merges objects either in a container or altogether.
     *
     * @param objects the {@link ObjectCollection} to merge
     * @param mergeFunc a function that merges a collection of objects together (changes the
     *     collection in place)
     * @return the merged {@link ObjectCollection}
     * @throws OperationFailedException if the merge operation fails
     */
    protected ObjectCollection mergeMultiplex(ObjectCollection objects, MergeObjects mergeFunc)
            throws OperationFailedException {

        // To avoid changing the original
        ObjectCollection objectsToMerge = objects.duplicateShallow();

        try {
            Optional<ObjectCollection> container = containerOptional();
            if (container.isPresent()) {
                return mergeInContainer(mergeFunc, objectsToMerge, container.get());
            } else {
                return mergeAll(mergeFunc, objectsToMerge);
            }
        } catch (ProvisionFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Gets an optional {@link Resolution} from the dimensions provider.
     *
     * @return an {@link Optional} containing a {@link Resolution} if available, empty otherwise
     * @throws OperationFailedException if retrieving the resolution fails
     */
    private Optional<Resolution> resolutionOptional() throws OperationFailedException {
        try {
            return OptionalProviderFactory.create(dimensions).flatMap(Dimensions::resolution);
        } catch (ProvisionFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Merges all objects in the collection.
     *
     * @param merger the {@link MergeObjects} function to use for merging
     * @param objects the {@link ObjectCollection} to merge
     * @return the merged {@link ObjectCollection}
     * @throws OperationFailedException if the merge operation fails
     */
    private static ObjectCollection mergeAll(MergeObjects merger, ObjectCollection objects)
            throws OperationFailedException {
        return ObjectCollectionFactory.of(merger.mergeObjects(objects));
    }

    /**
     * Merges objects within a container.
     *
     * @param merger the {@link MergeObjects} function to use for merging
     * @param objects the {@link ObjectCollection} to merge
     * @param containerObjects the container {@link ObjectCollection}
     * @return the merged {@link ObjectCollection}
     * @throws OperationFailedException if the merge operation fails
     */
    private static ObjectCollection mergeInContainer(
            MergeObjects merger, ObjectCollection objects, ObjectCollection containerObjects)
            throws OperationFailedException {

        // All matched objects
        Stream<ObjectCollection> matchesStream =
                MatcherIntersectionHelper.matchIntersectingObjects(containerObjects, objects)
                        .stream()
                        .map(MatchedObject::getMatches);

        return ObjectCollectionFactory.flatMapFrom(
                matchesStream, OperationFailedException.class, merger::mergeObjects);
    }
}
