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
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;

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
    public ObjectCollection get() throws ProvisionFailedException {

        try {
            PermutationSetter ps = permuteProperty.createSetter(objects);

            return createPermutedObjects(ps, streamFromIterator(permuteProperty.propertyValues()));
        } catch (PermutationSetterException | OperationFailedException e1) {
            throw new ProvisionFailedException("Cannot create a permutation setter", e1);
        }
    }

    private ObjectCollection createPermutedObjects(PermutationSetter setter, Stream<?> propVals)
            throws ProvisionFailedException {
        return ObjectCollectionFactory.flatMapFrom(
                propVals, CreateException.class, propVal -> objectsForPermutation(setter, propVal));
    }

    private ObjectCollection objectsForPermutation(PermutationSetter setter, Object propVal)
            throws ProvisionFailedException {
        // We permute a duplicate, so as to keep the original values
        ObjectCollectionProvider provider = objects.duplicateBean();
        try {
            setter.setPermutation(provider, propVal);
        } catch (PermutationSetterException e) {
            throw new ProvisionFailedException(
                    "Cannot set permutation on an object-mask-provider", e);
        }

        // We init after the permutation, as we might be changing a reference
        try {
            provider.initRecursive(getInitialization(), getLogger());
        } catch (InitException e) {
            throw new ProvisionFailedException(e);
        }

        return provider.get();
    }

    /** Converts an iterator to a stream */
    private static <T> Stream<T> streamFromIterator(Iterator<T> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }
}
