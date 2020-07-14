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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectCollection;

import lombok.Getter;
import lombok.Setter;

public class ObjMaskProviderIfParamEqual extends ObjectCollectionProvider {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private ObjectCollectionProvider objectsEqual;
	
	@BeanField @Getter @Setter
	private ObjectCollectionProvider objectsNotEqual;
	
	@BeanField @Getter @Setter
	private KeyValueParamsProvider keyValueParamsProvider;
	
	@BeanField @Getter @Setter
	private String key;
	
	@BeanField @Getter @Setter
	private String value;
	// END BEAN PROPERTIES

	@Override
	public ObjectCollection create() throws CreateException {
		
		KeyValueParams kvp = keyValueParamsProvider.create();
		
		if (kvp==null) {
			throw new CreateException("No keyValueParams");
		}
		
		String valFromProp = kvp.getProperty(key);
		
		if (valFromProp==null) {
			throw new CreateException("valFromProp is null");
		}		
		
		if( valFromProp.equals(value)) {
			return objectsEqual.create();
		} else {
			return objectsNotEqual.create();
		}
	}
}
