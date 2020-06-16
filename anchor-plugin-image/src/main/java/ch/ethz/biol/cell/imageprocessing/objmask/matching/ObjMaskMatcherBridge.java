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


import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.objmask.match.ObjMaskMatcher;
import org.anchoranalysis.image.objectmask.ObjectMaskCollection;
import org.anchoranalysis.image.objmask.match.ObjWithMatches;

// Matches to another object, and then uses that object to bridge to another
public class ObjMaskMatcherBridge extends ObjMaskMatcher {
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskMatcher bridgeMatcher;		// What we use as an intermediatery
	
	@BeanField
	private ObjMaskMatcher objMaskMatcher;		// Final objMaskMatcher
	// END BEAN PROPERTIES

	@Override
	public List<ObjWithMatches> findMatch(ObjectMaskCollection sourceObjs)
			throws OperationFailedException {
		
		List<ObjWithMatches> bridgeMatches = bridgeMatcher.findMatch(sourceObjs);
		
		ObjectMaskCollection bridgeObjs = new ObjectMaskCollection();
		for( ObjWithMatches owm : bridgeMatches ) {
			
			if (owm.getMatches().size()==0) {
				throw new OperationFailedException("At least one object has no match. One is needed");
			}
			
			if (owm.getMatches().size()>1) {
				throw new OperationFailedException("At least one object has multiple matches. Only one is allowed.");
			}
		}
		
		return objMaskMatcher.findMatch(bridgeObjs);
	}
	
	public ObjMaskMatcher getBridgeMatcher() {
		return bridgeMatcher;
	}

	public void setBridgeMatcher(ObjMaskMatcher bridgeMatcher) {
		this.bridgeMatcher = bridgeMatcher;
	}

	public ObjMaskMatcher getObjMaskMatcher() {
		return objMaskMatcher;
	}

	public void setObjMaskMatcher(ObjMaskMatcher objMaskMatcher) {
		this.objMaskMatcher = objMaskMatcher;
	}
}
