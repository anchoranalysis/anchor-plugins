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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ops.ObjectMaskMerger;

// Merges an object in each list item by item
//
// Requires each provider to return the same number of items
//
public class ObjMaskProviderMergeItemByItem extends ObjectCollectionProvider {

	// START BEAN PROPERTIES
	@BeanField
	private ObjectCollectionProvider objs1;
	
	@BeanField
	private ObjectCollectionProvider objs2;
	// END BEAN PROPERTIES
		
	@Override
	public ObjectCollection create() throws CreateException {

		ObjectCollection first = objs1.create();
		ObjectCollection second = objs2.create();
		
		if (first.size()!=second.size()) {
			throw new CreateException( String.format("Both object-providers must have the same number of items, currently %d and %d", first.size(), second.size() ));
		}
		
		return ObjectCollectionFactory.mapFromRange(
			0,
			first.size(),
			index->	ObjectMaskMerger.merge(
				first.get(index),
				second.get(index)
			)
		);
	}

	public ObjectCollectionProvider getObjs1() {
		return objs1;
	}

	public void setObjs1(ObjectCollectionProvider objs1) {
		this.objs1 = objs1;
	}

	public ObjectCollectionProvider getObjs2() {
		return objs2;
	}

	public void setObjs2(ObjectCollectionProvider objs2) {
		this.objs2 = objs2;
	}



}
