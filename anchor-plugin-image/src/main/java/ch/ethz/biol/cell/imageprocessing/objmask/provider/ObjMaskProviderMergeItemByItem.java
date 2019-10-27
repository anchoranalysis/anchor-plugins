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
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.ops.ObjMaskMerger;

// Merges an object in each list item by item
//
// Requires each provider to return the same number of items
//
public class ObjMaskProviderMergeItemByItem extends ObjMaskProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objMaskProvider1;
	
	@BeanField
	private ObjMaskProvider objMaskProvider2;
	// END BEAN PROPERTIES
		
	@Override
	public ObjMaskCollection create() throws CreateException {

		ObjMaskCollection objs1 = objMaskProvider1.create();
		ObjMaskCollection objs2 = objMaskProvider2.create();
		
		if (objs1.size()!=objs2.size()) {
			throw new CreateException( String.format("Both objProviders must have the same number of items, currently %d and %d", objs1.size(), objs2.size() ));
		}
		
		ObjMaskCollection out = new ObjMaskCollection();
		
		for( int i=0; i<objs1.size(); i++) {
			ObjMask om1 = objs1.get(i);
			ObjMask om2 = objs2.get(i);
			
			out.add( ObjMaskMerger.merge(om1, om2) );
		}
		
		return out;
	}

	public ObjMaskProvider getObjMaskProvider1() {
		return objMaskProvider1;
	}

	public void setObjMaskProvider1(ObjMaskProvider objMaskProvider1) {
		this.objMaskProvider1 = objMaskProvider1;
	}

	public ObjMaskProvider getObjMaskProvider2() {
		return objMaskProvider2;
	}

	public void setObjMaskProvider2(ObjMaskProvider objMaskProvider2) {
		this.objMaskProvider2 = objMaskProvider2;
	}


}
