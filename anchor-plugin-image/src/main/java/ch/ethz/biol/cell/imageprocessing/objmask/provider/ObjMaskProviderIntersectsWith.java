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

/**
 * Returns the only the objects from objMaskProvider that intersect
 *   with at least one object in objMaskProviderContainer
 *   
 * @author feehano
 *
 */
public class ObjMaskProviderIntersectsWith extends ObjMaskProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objMaskProvider;
	
	@BeanField
	private ObjMaskProvider objMaskProviderContainer;
	
	@BeanField
	private boolean inverse=false;	// If set, we return the objects that DO NOT intersect
	// END BEAN PROPERTIES
	
	private boolean doesObjIntersect( ObjMask om, ObjMaskCollection container ) {
		
		for( ObjMask omCompare : container ) {
			if(om.hasIntersectingPixels(omCompare)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	@Override
	public ObjMaskCollection create() throws CreateException {
		
		ObjMaskCollection objs = objMaskProvider.create();
		ObjMaskCollection objsContainer = objMaskProviderContainer.create();
		
		ObjMaskCollection out = new ObjMaskCollection();
		
		for( ObjMask om : objs ) {
			
			boolean intersection = doesObjIntersect(om,objsContainer);
			
			if (inverse) {
				if (!intersection) {
					out.add(om);
				}				
			} else {
				if (intersection) {
					out.add(om);
				}
			}
		}

		return out;
	}

	public ObjMaskProvider getObjMaskProvider() {
		return objMaskProvider;
	}

	public void setObjMaskProvider(ObjMaskProvider objMaskProvider) {
		this.objMaskProvider = objMaskProvider;
	}


	public ObjMaskProvider getObjMaskProviderContainer() {
		return objMaskProviderContainer;
	}

	public void setObjMaskProviderContainer(ObjMaskProvider objMaskProviderContainer) {
		this.objMaskProviderContainer = objMaskProviderContainer;
	}

	public boolean isInverse() {
		return inverse;
	}

	public void setInverse(boolean inverse) {
		this.inverse = inverse;
	}

}
