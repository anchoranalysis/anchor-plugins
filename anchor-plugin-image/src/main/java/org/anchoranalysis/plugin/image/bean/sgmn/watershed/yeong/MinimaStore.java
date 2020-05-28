package org.anchoranalysis.plugin.image.bean.sgmn.watershed.yeong;

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


import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

import ch.ethz.biol.cell.sgmn.objmask.ObjMaskChnlUtilities;

// Stores minima points
class MinimaStore {
		
	private List<Minima> list = new ArrayList<>();

	/** Adds a new minima, but duplicates the point (which is mutable and may change during iteration) before adding */
	public void addDuplicated( Point3i pnt ) {
		list.add(
			new Minima(
				new Point3i(pnt)
			)
		);
	}
	
	public void add( List<Point3i> pnts ) {
		list.add( new Minima(pnts) );
	}
	
	public ObjMaskCollection createObjMasks() throws CreateException {
		
		ObjMaskCollection out = new ObjMaskCollection();
		for( Minima m : list ) {
  
			out.add(
				ObjMaskChnlUtilities.createObjMaskFromPoints( m.getListPnts() )
			);
		}
		return out;
	}
	
	public int size() {
		return list.size();
	}
	
//	public Minima addEmptyMinima() {
//		Minima m = new Minima();
//		list.add(m);
//		return m;
//	}
}
