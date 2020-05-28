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
import java.util.Optional;
import java.util.function.Function;

import org.anchoranalysis.core.functional.OptionalUtilities;

public class ParsedFilePathBag implements Iterable<FileDetails> {
	
	private List<FileDetails> list = new ArrayList<>();

	// chnlNum and sliceNum can be null, indicating that we don't know the values
	public void add( FileDetails fileDetails ) {
		list.add( fileDetails );
	}

	public Iterator<FileDetails> iterator() {
		return list.iterator();
	}
	
	public Optional<IntegerRange> rangeChnlNum() {
		return range( fd -> fd.getChnlNum() );
	}
	
	public Optional<IntegerRange> rangeSliceNum() {
		return range( fd -> fd.getSliceNum() );
	}
	
	public Optional<IntegerRange> rangeTimeIndex() {
		return range( fd -> fd.getTimeIndex() );
	}
		
	private Optional<IntegerRange> range( Function<FileDetails,Optional<Integer>> func ) {
		return OptionalUtilities.mapBoth(
			getMin( func ),
			getMax( func ),
			(min, max) -> new IntegerRange(min, max)
		);
	}
	
	private Optional<Integer> getMax( Function<FileDetails,Optional<Integer>> func ) {
		
		Optional<Integer> max = Optional.empty();
		for (FileDetails fd : list) {
			Optional<Integer> val = func.apply(fd); 
			
			if (!val.isPresent()) {
				continue;
			}
			
			if (!max.isPresent() || val.get()>max.get()) {
				max = val;
			}
		}
		return max;
	}
	
	public Optional<Integer> getMin( Function<FileDetails,Optional<Integer>> func ) {
		
		Optional<Integer> min = Optional.empty();
		for (FileDetails fd : list) {
			Optional<Integer> val = func.apply(fd);
			
			if (!val.isPresent()) {
				continue;
			}
			
			if (!min.isPresent() || val.get() < min.get()) {
				min = val;
			}
		}
		return min;
	}

	public int size() {
		return list.size();
	}	
}
