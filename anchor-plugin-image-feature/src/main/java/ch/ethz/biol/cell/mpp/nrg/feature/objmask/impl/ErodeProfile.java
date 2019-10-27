package ch.ethz.biol.cell.mpp.nrg.feature.objmask.impl;

/*-
 * #%L
 * anchor-plugin-image-feature
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

import java.util.ArrayList;
import java.util.List;

/**
 * A series of 1-pixel-wide "film-like" objects found are from successive erosions from the exterior 
 * 
 * A feature is calculated on each object to form a function f(x)
 *   where x    is an index-integer, 0,1,2,3 etc. 
 *     and f(x) is the feature-value associated with that index
 * 
 * @author Owen
 *
 */
public class ErodeProfile {

	private int minKey;
	private int maxKey;
	private List<Double> savedVals;

	/**
	 * Adds an index and its associated feature-value
	 * 
	 * Keys must be added in increments of 1
	 * 
	 * The initial key can be any value
	 * 
	 * @param index
	 * @param featureValue
	 */
	public void add( int index, double featureValue ) {
		
		if (savedVals==null) {
			savedVals = new ArrayList<>();
			minKey = index;
		} else {
			if (index!=(maxKey+1)) {
				throw new IllegalArgumentException(
					String.format("Index values must be entered in increments of one. The previous value was %d. You are trying to add %d",index,featureValue)
				);
			}
		}
		
		savedVals.add(featureValue);
		maxKey = index;
	}
	
	public double get(int index) {
		return savedVals.get( index - minKey );
	}

	public int indexForMaxVal() {
		return indexForMaxVal( savedVals.size() );
	}
	
	public int indexForMaxVal( int maxWindowSize ) {
		
		double maxVal = Double.MIN_VALUE;
		int maxInd = -1;
		
		for( int i=0; i<maxWindowSize; i++ ) {
			double val = savedVals.get(i);
			if (maxInd==-1) {
				maxInd = i;
				maxVal = val;
			} else if (val > maxVal) {
				maxInd = i;
				maxVal = val;
			}
		}
		
		// We return the value with any initial shift
		return maxInd + minKey;
	}
	
	public int indexMin() {
		double minVal = Double.MAX_VALUE;
		int minInd = -1;
		
		for( int i=0; i<savedVals.size(); i++ ) {
			double val = savedVals.get(i);
			if (minInd==-1) {
				minInd = i;
				minVal = val;
			} else if (val < minVal) {
				minInd = i;
				minVal = val;
			}
		}
		
		// We return the value with any initial shift
		return minInd + minKey;
	}
	
	// We find the first local minima after afterIndex
	public int indexForNextLocalMin( int afterIndex ) {
	
		double prevVal = savedVals.get(afterIndex-minKey); 
		
		for( int i=(afterIndex+1); i<savedVals.size(); i++ ) {
			
			double val = savedVals.get(i);
			
			double delta = val-prevVal;
			
			if (delta>=0) {
				// We've found the local minima
				return i - 1 + minKey;
			}
			
			prevVal = val;
		}
		
		// We've reached the end, and haven't found any minima, so we take the final index
		return savedVals.size() - 1 + minKey;
	}

	
}
