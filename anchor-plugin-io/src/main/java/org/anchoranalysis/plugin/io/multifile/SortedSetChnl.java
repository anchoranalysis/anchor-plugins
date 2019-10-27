package org.anchoranalysis.plugin.io.multifile;

/*
 * #%L
 * anchor-io
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


import java.util.Iterator;
import java.util.TreeMap;

public class SortedSetChnl implements Comparable<SortedSetChnl>, Iterable<SortedSetSlice>  {

	private int chnlNum;
	
	private TreeMap<Integer,SortedSetSlice> slices = null;

	public SortedSetChnl(int chnlNum) {
		super();
		this.chnlNum = chnlNum;
		this.slices = new TreeMap<>();
	}

	public int numSlices() {
		return this.slices.size();
	}
	
	public void add( int sliceNum, String filePath ) {
		
		SortedSetSlice slice = slices.get(sliceNum);
		
		if (slice==null) {
			slice = new SortedSetSlice(filePath, sliceNum);
			slices.put(sliceNum, slice);
		} else {
			slice.setFilePath(filePath);
			
			// We shouldn't ever have two channels with the same ID
			assert false;
		}
	}

	@Override
	public int compareTo(SortedSetChnl arg0) {
		
		if (chnlNum==arg0.chnlNum) {
			return 0;
		} else if (chnlNum<arg0.chnlNum) {
			return -1;
		} else {
			return 1;
		}
	}


	@Override
	public Iterator<SortedSetSlice> iterator() {
		return slices.values().iterator();
	}


	public int getChnlNum() {
		return chnlNum;
	}


	public void setChnlNum(int chnlNum) {
		this.chnlNum = chnlNum;
	}
}
