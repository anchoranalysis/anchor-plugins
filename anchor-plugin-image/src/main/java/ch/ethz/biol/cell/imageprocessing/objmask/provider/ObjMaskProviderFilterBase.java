package ch.ethz.biol.cell.imageprocessing.objmask.provider;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import java.util.Optional;

import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.object.ObjectFilter;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;

import lombok.Getter;
import lombok.Setter;

public abstract class ObjMaskProviderFilterBase extends ObjMaskProviderDimensionsOptional {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private ObjectFilter filter;
	
	@BeanField @OptionalBean @Getter @Setter
	private ObjectCollectionProvider objectsRejected;	// The rejected objects are put here (OPTIONAL)
	// END BEAN PROPERTIES
	
	@Override
	public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {
		return createFromObjects(
			objects,
			OptionalFactory.create(objectsRejected),
			createDims()
		);
	}
	
	protected ObjectCollection filter(
		ObjectCollection objects,
		Optional<ImageDimensions> dim,
		Optional<ObjectCollection> objectsRejected
	) throws CreateException {
		try {
			return filter.filter(objects, dim, objectsRejected);
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}
	
	protected abstract ObjectCollection createFromObjects(
		ObjectCollection objects,
		Optional<ObjectCollection> objectsRejected,
		Optional<ImageDimensions> dim
	) throws CreateException;
}
