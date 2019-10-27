package org.anchoranalysis.plugin.io.multifile;

/*-
 * #%L
 * anchor-plugin-io
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

import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.stack.Stack;

/**
 * Remembers the different sizes among the files
 * 
 * @author FEEHANO
 *
 */
public class SizeExtnts {
	private Integer sizeX = null;
	private Integer sizeY = null;
	private IntegerRange rangeZ = null;
	private IntegerRange rangeC = null;
	private IntegerRange rangeT = null;
	
	// Assumes numbering starts from 0
	public SizeExtnts( ParsedFilePathBag fileBag ) {
		this.rangeZ = fileBag.rangeSliceNum();
		this.rangeC = fileBag.rangeChnlNum();
		this.rangeT = fileBag.rangeTimeIndex();
	}
	
	public boolean hasNecessaryExtnts() {
		return rangeC.hasSizeDefined() && rangeZ.hasSizeDefined() && rangeT.hasSizeDefined();
	}
	
	public void populateMissingFromArbitrarySlice( Stack stackArbitrarySlice ) {
		
		sizeX = stackArbitrarySlice.getDimensions().getX();
		sizeY = stackArbitrarySlice.getDimensions().getY();
		
		if (!rangeC.hasSizeDefined()) {
			rangeC = new IntegerRange(stackArbitrarySlice.getNumChnl());
		}
		
		if (!rangeZ.hasSizeDefined()) {
			rangeZ = new IntegerRange(stackArbitrarySlice.getDimensions().getZ());
		}
		
		if (!rangeT.hasSizeDefined()) {
			// If there's no indexes associated with the files, we assume there's a single index
			rangeT = new IntegerRange(1);
		}
	}
	

	public Extent toExtnt() {
		 return new Extent(sizeX,sizeY,rangeZ.getSize());
	}

	public IntegerRange getRangeZ() {
		return rangeZ;
	}

	public IntegerRange getRangeC() {
		return rangeC;
	}

	public IntegerRange getRangeT() {
		return rangeT;
	}


}
