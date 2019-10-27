package ch.ethz.biol.cell.imageprocessing.objmask.matching;

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


import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.objmask.match.ObjMaskMatcher;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.match.ObjWithMatches;

// Always matches a single object
public class ObjMaskMatcherSingleton extends ObjMaskMatcher {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objMaskProvider;
	// END BEAN PROPERTIES
	
	@Override
	public List<ObjWithMatches> findMatch(ObjMaskCollection sourceObjs)
			throws OperationFailedException {

		ObjMaskCollection omc;
		try {
			omc = objMaskProvider.create();
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
		
		if (omc.size()==0) {
			throw new OperationFailedException("No objects provided");
		}
		if (omc.size()>1) {
			throw new OperationFailedException("More than one objects provided");
		}
		
		ObjMask omMatch;
		try {
			omMatch = objMaskProvider.create().get(0);
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
		
		ArrayList<ObjWithMatches> list = new ArrayList<>();
		
		for( ObjMask om : sourceObjs ) {
			ObjWithMatches owm = new ObjWithMatches(om);
			owm.getMatches().add(omMatch);
			list.add( owm );
		}
		
		return list;
	}

	public ObjMaskProvider getObjMaskProvider() {
		return objMaskProvider;
	}

	public void setObjMaskProvider(ObjMaskProvider objMaskProvider) {
		this.objMaskProvider = objMaskProvider;
	}


}
