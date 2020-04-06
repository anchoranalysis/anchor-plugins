package ch.ethz.biol.cell.mpp.nrg.feature.objmask.sharedobjects;

/*
 * #%L
 * anchor-plugin-image-feature
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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMaskSharedObjects;
import org.anchoranalysis.image.feature.init.FeatureInitParamsImageInit;
import org.anchoranalysis.image.index.rtree.ObjMaskCollectionRTree;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

public abstract class FeatureAmongObjMaskCollection extends FeatureObjMaskSharedObjects {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	/**
	 * ID for the particular ObjMaskCollection
	 */
	@BeanField
	private String id="";
	
	@BeanField
	private double valueNoObjects = Double.NaN;
	// END BEAN PROPERTIES

	private ObjMaskCollection searchObjs;
	private ObjMaskCollectionRTree bboxRTree = null;
	
	@Override
	public void beforeCalcCast(FeatureInitParamsImageInit params) throws InitException {
	
		try {
			this.searchObjs = params.getSharedObjects().getObjMaskCollection().getException(id);
		} catch (NamedProviderGetException e) {
			throw new InitException(e.summarize());
		};
	}
	
	protected ObjMaskCollectionRTree bboxRTree() {
		
		// First time it's hit we calculate the bboxRTree
		if (bboxRTree==null) {
			this.bboxRTree = new ObjMaskCollectionRTree( searchObjs );
		}
		return bboxRTree;
	}
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getValueNoObjects() {
		return valueNoObjects;
	}


	public void setValueNoObjects(double valueNoObjects) {
		this.valueNoObjects = valueNoObjects;
	}

	protected ObjMaskCollection getSearchObjs() {
		return searchObjs;
	}

}
