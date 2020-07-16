/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.plugin.image.bean.object.match.MatcherIntersectionHelper;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithContainer;

/** A base class for algorithms that merge object-masks */
public abstract class MergeBase extends ObjectCollectionProviderWithContainer {

    // START BEAN PROPERTIES
    /* Image-resolution */
    @BeanField @OptionalBean @Getter @Setter private ImageDimProvider dim;
    // END BEAN PROPERTIES

    @FunctionalInterface
    protected static interface MergeObjects {
        ObjectCollection mergeObjects(ObjectCollection objects) throws OperationFailedException;
    }

    protected Optional<ImageResolution> calcResOptional() throws OperationFailedException {
        try {
            return OptionalFactory.create(dim).map(ImageDimensions::getRes);
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    protected ImageResolution calcResRequired() throws OperationFailedException {
        return calcResOptional()
                .orElseThrow(
                        () ->
                                new OperationFailedException(
                                        "This algorithm requires an image-resolution to be set via resProvider"));
    }

    /**
     * Merges either in a container, or altogether
     *
     * @param objects
     * @param mergeFunc a function that merges a collection of objects together (changes the
     *     collection in place)
     * @return
     * @throws OperationFailedException
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
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private static ObjectCollection mergeAll(MergeObjects merger, ObjectCollection objects)
            throws OperationFailedException {
        return ObjectCollectionFactory.from(merger.mergeObjects(objects));
    }

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
