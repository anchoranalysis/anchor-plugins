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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.permute.property.PermuteProperty;
import org.anchoranalysis.bean.permute.setter.PermutationSetter;
import org.anchoranalysis.bean.permute.setter.PermutationSetterException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProviderOne;
import org.anchoranalysis.image.objectmask.ObjectMaskCollection;

/**
 * Permutes some changes over an {@link ObjMaskProvider} and collects all the results in an {@link ObjectMaskCollection}
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
	public ObjectMaskCollection create() throws CreateException {
				
		try {
			PermutationSetter ps = permuteProperty.createSetter(objs);
			
			return createPermutedObjs(
				ps,
				permuteProperty.propertyValues()	
			);
		} catch (PermutationSetterException e1) {
			throw new CreateException("Cannot create a permutation setter", e1);
		}
	}
	
	private ObjectMaskCollection createPermutedObjs( PermutationSetter setter, Iterator<?> vals ) throws CreateException {
		ObjectMaskCollection out = new ObjectMaskCollection();
		try {
			while( vals.hasNext() ) {
				Object propVal = vals.next();
				assert(propVal!=null);
		
				// We permute a duplicate, so as to keep the original values
				ObjMaskProvider provider = objs.duplicateBean();
				setter.setPermutation(provider, propVal);

				// We init after the permutation, as we might be changing a reference
				provider.initRecursive( getSharedObjects(), getLogger() );

				out.addAll(
					provider.create()
				);
			}
			
		} catch (PermutationSetterException e) {
			throw new CreateException("Cannot set permutation on an object-mask-provider", e);
		} catch (InitException e) {
			throw new CreateException(e);
		}
		
		return out;
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
}
