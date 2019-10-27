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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class ParsedFilePathBag implements Iterable<FileDetails> {
	
	private List<FileDetails> list = new ArrayList<>();

	// chnlNum and sliceNum can be null, indicating that we don't know the values
	public void add( FileDetails fileDetails ) {
		list.add( fileDetails );
	}

	public Iterator<FileDetails> iterator() {
		return list.iterator();
	}
	
	public IntegerRange rangeChnlNum() {
		return range( fd -> fd.getChnlNum() );
	}
	
	public IntegerRange rangeSliceNum() {
		return range( fd -> fd.getSliceNum() );
	}
	
	public IntegerRange rangeTimeIndex() {
		return range( fd -> fd.getTimeIndex() );
	}
		
	private IntegerRange range( Function<FileDetails,Integer> func ) {
		return new IntegerRange(
			getMin( func ),
			getMax( func )
		);
	}
	
	private Integer getMax( Function<FileDetails,Integer> func ) {
		
		Integer max = null;
		for (FileDetails fd : list) {
			Integer val = func.apply(fd); 
			if (max==null || val>max) {
				max = val;
			}
		}
		return max;
	}
	
	public Integer getMin( Function<FileDetails,Integer> func ) {
		
		Integer min = null;
		for (FileDetails fd : list) {
			Integer val = func.apply(fd); 
			if (min==null || val<min) {
				min = val;
			}
		}
		return min;
	}

	public int size() {
		return list.size();
	}	
}
