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

public class ObjMaskProviderNotIn extends ObjMaskProvider {

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objs;
	
	@BeanField
	private ObjMaskProvider objsContainer;
	// END BEAN PROPERTIES
	
	
	private boolean isObjIn( ObjMask om, ObjMaskCollection container ) {
		
		for( ObjMask omCompare : container ) {
			if(om.equals(omCompare)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	@Override
	public ObjMaskCollection create() throws CreateException {
		
		ObjMaskCollection objsCollection = objs.create();
		ObjMaskCollection objsContainerCollection = objsContainer.create();
		
		ObjMaskCollection out = new ObjMaskCollection();
		
		for( ObjMask om : objsCollection ) {
			
			if (!isObjIn(om,objsContainerCollection)) {
				out.add(om);
			}
		}

		return out;
	}

	public ObjMaskProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}


	public ObjMaskProvider getObjsContainer() {
		return objsContainer;
	}


	public void setObjsContainer(ObjMaskProvider objsContainer) {
		this.objsContainer = objsContainer;
	}

}
