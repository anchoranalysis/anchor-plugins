package org.anchoranalysis.plugin.mpp.bean.proposer.orientation;

import org.anchoranalysis.anchor.mpp.bean.bound.OrientableBounds;

/*-
 * #%L
 * anchor-plugin-mpp
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

import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.orientation.Orientation;

public class Random extends OrientationProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6739151761002161746L;
	
	@Override
	public Orientation propose(Mark mark,	ImageDim dim, RandomNumberGenerator re, ErrorNode errorNode) {
		
		try {
			if (getSharedObjects().getMarkBounds()==null || !(getSharedObjects().getMarkBounds() instanceof OrientableBounds) ) {
				errorNode.add("markBounds must be non-null and of type OrientableBounds");
				return null;
			}
			
			return ((OrientableBounds) getSharedObjects().getMarkBounds()).randomOrientation(re, dim.getRes());
		} catch (GetOperationFailedException e) {
			errorNode.add(e);
			return null;
		}
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return true;
	}
}
