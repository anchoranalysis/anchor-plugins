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

import org.anchoranalysis.core.geometry.Point3i;


class EqualVoxelsPlateau {

		private List<PointWithNghb> ptsEdge = new ArrayList<>();
		private List<Point3i> ptsInner = new ArrayList<>();
		
		public void addEdge( Point3i pnt, int nghbIndex ) {
			assert( nghbIndex >= 0 );
			ptsEdge.add( new PointWithNghb(pnt, nghbIndex) );
		}
		
		public void addInner( Point3i pnt ) {
			// We make we duplicate, as pnt is coming from an iterator and is mutable
			ptsInner.add(
				new Point3i(pnt)
			);
		}
		
		public boolean hasPoints() {
			return ptsEdge.size()>0 || ptsInner.size()>0;
		}
		
		public boolean isOnlyEdge() {
			return ptsInner.size()==0 && ptsEdge.size()>0; 
		}
		
		public boolean isOnlyInner() {
			return ptsEdge.size()==0 && ptsInner.size()>0; 
		}
		
		// Returns the existing list of edges, and creates a new empty list in its place
//		public List<PointWithNghb> releaseEdgePts() {
//			List<PointWithNghb> ptsEdgePrev = ptsEdge;
//			ptsEdge = new ArrayList<PointWithNghb>();
//			return ptsEdgePrev;
//		}
		
		// EFFICIENCY, rewrite as two separate lists, avoids having to make new lists
		public List<Point3i> ptsEdgeAsPoints() {
			List<Point3i> ptsEdgeOut = new ArrayList<>();
			
			for( PointWithNghb pwn : ptsEdge ) {
				ptsEdgeOut.add( pwn.getPnt() );
			}
			return ptsEdgeOut;
		}
		
		public List<PointWithNghb> getPtsEdge() {
			return ptsEdge;
		}
		public List<Point3i> getPtsInner() {
			return ptsInner;
		}

		public boolean hasNullItems() {
			
			for( Point3i p : ptsInner ) {
				if(p==null) {
					return true;
				}
			}
			
			for( PointWithNghb pwn : ptsEdge ) {
				if(pwn==null) {
					return true;
				}
			}
			return false;
		}
		public int size() {
			return ptsEdge.size() + ptsInner.size();
		}
	}