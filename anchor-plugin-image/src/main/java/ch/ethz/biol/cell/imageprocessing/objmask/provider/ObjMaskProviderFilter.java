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
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.objmask.filter.ObjMaskFilter;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

public class ObjMaskProviderFilter extends ObjMaskProviderDimensionsOptional {

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskFilter objMaskFilter;
	
	@BeanField
	private ObjMaskProvider objs;
	
	@BeanField @OptionalBean
	private ObjMaskProvider objsRejected;	// The rejected objects are put here (OPTIONAL)
	// END BEAN PROPERTIES
	
	@Override
	public ObjMaskCollection create() throws CreateException {
		
		ObjMaskCollection in = objs.create();
		
		ObjMaskCollection out = new ObjMaskCollection();
		out.addAll( in );
		try {
			ImageDim dims = createDims();
			
			ObjMaskCollection omcRejected = objsRejected!=null ? objsRejected.create() : null;
				
			objMaskFilter.filter(out, dims, omcRejected);
			
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	
		return out;
	}

	public ObjMaskFilter getObjMaskFilter() {
		return objMaskFilter;
	}

	public void setObjMaskFilter(ObjMaskFilter objMaskFilter) {
		this.objMaskFilter = objMaskFilter;
	}

	public ObjMaskProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}

	public ObjMaskProvider getObjsRejected() {
		return objsRejected;
	}

	public void setObjsRejected(ObjMaskProvider objsRejected) {
		this.objsRejected = objsRejected;
	}

}
