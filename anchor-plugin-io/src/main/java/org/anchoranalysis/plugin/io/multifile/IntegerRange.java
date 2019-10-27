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

public class IntegerRange {
	
	private Integer size = null;
	private int deduct = 0;
	
	public IntegerRange( Integer maxNum ) {
		this.size = maxNum;
	}
	
	public IntegerRange( Integer minNum, Integer maxNum ) {

		if (maxNum!=null) {
			// We deduct the min
			deduct = minNum;
			this.size = maxNum - deduct + 1;
		}
	}
	
	/** Maps an original integer to it's index from (0..size] */ 
	public int index( int val ) {
		return val-deduct;
	}

	public int getSize() {
		assert(size!=null);
		return size;
	}
	
	public boolean hasSizeDefined() {
		return size!=null;
	}
}
