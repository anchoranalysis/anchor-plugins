package ch.ethz.biol.cell.imageprocessing.objmask.filter;

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


import java.util.HashSet;
import java.util.Set;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.objmask.filter.ObjMaskFilter;
import org.anchoranalysis.image.bean.objmask.filter.ObjMaskFilterList;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

public class ObjMaskFilterOr extends ObjMaskFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1049151951022876436L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskFilterList list = null;
	// END BEAN PROPERTIES
	
	@Override
	public void filter(ObjMaskCollection objs, ImageDim dim, ObjMaskCollection objsRejected) throws OperationFailedException {
		
		Set<ObjMask> set = new HashSet<ObjMask>();
		
		for (ObjMaskFilter indFilter : list) {
			ObjMaskCollection objsDup = objs.duplicate();
			indFilter.filter(objsDup, dim, objsRejected);
			set.addAll(objsDup.asList());
		}

		objs.clear();
		objs.addAll(set);
	}

	public ObjMaskFilterList getList() {
		return list;
	}

	public void setList(ObjMaskFilterList list) {
		this.list = list;
	}


}
