package ch.ethz.biol.cell.mpp.bound;

/*
 * #%L
 * anchor-plugin-mpp
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


import java.util.HashMap;

class AngleStore<StoreType> {
	
	private int precisionMultiplier;
	
	private HashMap<Integer,StoreType> map = new HashMap<>(); 

	public AngleStore( int precisionMultiplier ) {
		this.precisionMultiplier = precisionMultiplier;
	}
	
	public int cnvrtToIndex( double angle ) {
		return (int) (angle * precisionMultiplier);
	}
	
	public double cnvrtToAngle( int index ) {
		double dbl = index;
		return dbl / precisionMultiplier;
	}
	
	public StoreType get( int index ) {
		return map.get( index );
	}
	
	public void put( int index, StoreType item ) {
		map.put( index, item );
	}
	
}