package ch.ethz.biol.cell.imageprocessing.objmask.provider;

/*
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.permute.property.PermuteProperty;
import org.anchoranalysis.bean.permute.setter.PermutationSetter;
import org.anchoranalysis.bean.permute.setter.PermutationSetterException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderOne;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;

import lombok.Getter;
import lombok.Setter;

/**
 * Permutes some changes over an {@link ObjectCollectionProvider} and collects all the results in an {@link ObjectCollection}
 * 
 * We deliberately do not inherit from {@link ObjectCollectionProviderOne} as we not using the {@link ObjectCollectionProvider} in the same way.
 * 
 * @author Owen Feehan
 *
 */
public class ObjMaskProviderPermute extends ObjectCollectionProvider {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private ObjectCollectionProvider objects;
	
	@BeanField @Getter @Setter
	private PermuteProperty<?> permuteProperty;
	// END BEAN PROPERTIES

	@Override
	public ObjectCollection create() throws CreateException {
				
		try {
			PermutationSetter ps = permuteProperty.createSetter(objects);
			
			return createPermutedObjs(
				ps,
				streamFromIterator( permuteProperty.propertyValues() )
			);
		} catch (PermutationSetterException e1) {
			throw new CreateException("Cannot create a permutation setter", e1);
		}
	}
	
	private ObjectCollection createPermutedObjs( PermutationSetter setter, Stream<?> propVals ) throws CreateException {
		return ObjectCollectionFactory.flatMapFrom(
			propVals,
			CreateException.class,
			propVal -> objectsForPermutation(setter, propVal)
		);
	}
	
	private ObjectCollection objectsForPermutation(PermutationSetter setter, Object propVal) throws CreateException {
		// We permute a duplicate, so as to keep the original values
		ObjectCollectionProvider provider = objects.duplicateBean();
		try {
			setter.setPermutation(provider, propVal);
		} catch (PermutationSetterException e) {
			throw new CreateException("Cannot set permutation on an object-mask-provider", e);
		}
		
		// We init after the permutation, as we might be changing a reference
		try {
			provider.initRecursive( getInitializationParameters(), getLogger() );
		} catch (InitException e) {
			throw new CreateException(e);
		}

		return provider.create();
	}
	
	/** Converts an iterator to a stream */
	private static <T> Stream<T> streamFromIterator(Iterator<T> iterator) {
		return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator,Spliterator.ORDERED),
            false
        );
	}
}
