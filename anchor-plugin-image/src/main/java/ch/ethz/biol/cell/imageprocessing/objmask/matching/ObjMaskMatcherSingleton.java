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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.object.ObjectMatcher;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Specifies a single object that will always be the "match" for whatever source object.
 * 
 * @author Owen Feehan
 *
 */
public class ObjMaskMatcherSingleton extends ObjectMatcher {

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objs;
	// END BEAN PROPERTIES
	
	@Override
	public List<MatchedObject> findMatch(ObjectCollection sourceObjs) throws OperationFailedException {

		ObjectMask omMatch = determineMatch();
		
		return sourceObjs.stream().mapToList(
			om -> new MatchedObject(
				om,
				ObjectCollectionFactory.from(omMatch)
			) 
		);
	}
	
	private ObjectMask determineMatch() throws OperationFailedException {
		ObjectCollection objsCollection;
		try {
			objsCollection = objs.create();
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
		
		if (objsCollection.size()==0) {
			throw new OperationFailedException("No objects provided");
		}
		if (objsCollection.size()>1) {
			throw new OperationFailedException("More than one objects provided");
		}
		
		return objsCollection.get(0);
	}

	public ObjMaskProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}


}
