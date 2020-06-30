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
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProviderOne;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;

/**
 * Permutes some changes over an {@link ObjMaskProvider} and collects all the results in an {@link ObjectCollection}
 * 
 * We deliberately do not inherit from {@link ObjMaskProviderOne} as we not using the {@link ObjMaskProvider} in the same way.
 * 
 * @author Owen Feehan
 *
 */
public class ObjMaskProviderPermute extends ObjMaskProvider {

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objs;
	
	@BeanField
	private PermuteProperty<?> permuteProperty;
	// END BEAN PROPERTIES

	@Override
	public ObjectCollection create() throws CreateException {
				
		try {
			PermutationSetter ps = permuteProperty.createSetter(objs);
			
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
			propVal -> objsForPermutation(setter, propVal)
		);
	}
	
	private ObjectCollection objsForPermutation(PermutationSetter setter, Object propVal) throws CreateException {
		// We permute a duplicate, so as to keep the original values
		ObjMaskProvider provider = objs.duplicateBean();
		try {
			setter.setPermutation(provider, propVal);
		} catch (PermutationSetterException e) {
			throw new CreateException("Cannot set permutation on an object-mask-provider", e);
		}
		
		// We init after the permutation, as we might be changing a reference
		try {
			provider.initRecursive( getSharedObjects(), getLogger() );
		} catch (InitException e) {
			throw new CreateException(e);
		}

		return provider.create();
	}

	public ObjMaskProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}

	public PermuteProperty<?> getPermuteProperty() {
		return permuteProperty;
	}

	public void setPermuteProperty(PermuteProperty<?> permuteProperty) {
		this.permuteProperty = permuteProperty;
	}
	
	/** Converts an iterator to a stream */
	private static <T> Stream<T> streamFromIterator(Iterator<T> iterator) {
		return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator,Spliterator.ORDERED),
            false
        );
	}
}
