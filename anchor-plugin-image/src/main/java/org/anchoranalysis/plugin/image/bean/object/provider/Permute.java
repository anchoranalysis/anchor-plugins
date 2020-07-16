/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.permute.property.PermuteProperty;
import org.anchoranalysis.bean.permute.setter.PermutationSetter;
import org.anchoranalysis.bean.permute.setter.PermutationSetterException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;

/**
 * Permutes some changes over an {@link ObjectCollectionProvider} and collects all the results in an
 * {@link ObjectCollection}
 *
 * <p>We deliberately do not inherit from {@link ObjectCollectionProviderUnary} as we not using the
 * {@link ObjectCollectionProvider} in the same way.
 *
 * @author Owen Feehan
 */
public class Permute extends ObjectCollectionProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;

    @BeanField @Getter @Setter private PermuteProperty<?> permuteProperty;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection create() throws CreateException {

        try {
            PermutationSetter ps = permuteProperty.createSetter(objects);

            return createPermutedObjects(ps, streamFromIterator(permuteProperty.propertyValues()));
        } catch (PermutationSetterException e1) {
            throw new CreateException("Cannot create a permutation setter", e1);
        }
    }

    private ObjectCollection createPermutedObjects(PermutationSetter setter, Stream<?> propVals)
            throws CreateException {
        return ObjectCollectionFactory.flatMapFrom(
                propVals, CreateException.class, propVal -> objectsForPermutation(setter, propVal));
    }

    private ObjectCollection objectsForPermutation(PermutationSetter setter, Object propVal)
            throws CreateException {
        // We permute a duplicate, so as to keep the original values
        ObjectCollectionProvider provider = objects.duplicateBean();
        try {
            setter.setPermutation(provider, propVal);
        } catch (PermutationSetterException e) {
            throw new CreateException("Cannot set permutation on an object-mask-provider", e);
        }

        // We init after the permutation, as we might be changing a reference
        try {
            provider.initRecursive(getInitializationParameters(), getLogger());
        } catch (InitException e) {
            throw new CreateException(e);
        }

        return provider.create();
    }

    /** Converts an iterator to a stream */
    private static <T> Stream<T> streamFromIterator(Iterator<T> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }
}
