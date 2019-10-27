package org.anchoranalysis.bean.provider.objs.merge;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.match.ObjWithMatches;

import ch.ethz.biol.cell.imageprocessing.objmask.matching.ObjMaskMatchUtilities;

/** A base class for algorithms that merge obj-masks */
public abstract class ObjMaskProviderMergeBase extends ObjMaskProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objMaskProvider;
	
	@BeanField @Optional
	private ObjMaskProvider objMaskProviderContainer;
	// END BEAN PROPERTIES

	@FunctionalInterface
	protected static interface MergeObjs {
		ObjMaskCollection mergeObjs( ObjMaskCollection objs ) throws OperationFailedException;
	}
	
	/**
	 * Merges either in a container, or altogether
	 * 
	 * @param objs
	 * @param mergeFunc a function that merges a collection of objects together (changes the collection in place)
	 * @return
	 * @throws OperationFailedException
	 */
	protected ObjMaskCollection mergeMultiplex( ObjMaskCollection objs, MergeObjs mergeFunc ) throws OperationFailedException {
		
		// To avoid changing the original
		ObjMaskCollection objsToMerge = objs.duplicateShallow();
		
		if (objMaskProviderContainer==null) {
			return mergeAll(mergeFunc, objsToMerge);
		} else {
			try {
				
				ObjMaskCollection containerObjs = objMaskProviderContainer.create();
				return mergeInContainer(mergeFunc, objsToMerge, containerObjs);
				
			} catch (CreateException e) {
				throw new OperationFailedException(e);
			}
		}
	}
	
	private ObjMaskCollection mergeAll( MergeObjs merger, ObjMaskCollection objs) throws OperationFailedException {
		ObjMaskCollection out = new ObjMaskCollection();
		// We merge them all
		ObjMaskCollection mergedObjs = merger.mergeObjs(objs);
		out.addAll(mergedObjs);
		return out;
	}
	
	private ObjMaskCollection mergeInContainer( MergeObjs merger, ObjMaskCollection objs, ObjMaskCollection containerObjs) throws OperationFailedException {
		
		ObjMaskCollection out = new ObjMaskCollection();
				
		List<ObjWithMatches> matchList = ObjMaskMatchUtilities.matchIntersectingObjects( containerObjs, objs );
		
		for( ObjWithMatches owm : matchList ) {

			ObjMaskCollection mergedObjs = merger.mergeObjs( owm.getMatches() );
			out.addAll( mergedObjs );
		}
		return out;		
	}
		
	public ObjMaskProvider getObjMaskProviderContainer() {
		return objMaskProviderContainer;
	}

	public void setObjMaskProviderContainer(ObjMaskProvider objMaskProviderContainer) {
		this.objMaskProviderContainer = objMaskProviderContainer;
	}
	
	public ObjMaskProvider getObjMaskProvider() {
		return objMaskProvider;
	}

	public void setObjMaskProvider(ObjMaskProvider objMaskProvider) {
		this.objMaskProvider = objMaskProvider;
	}
}
