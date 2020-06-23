package org.anchoranalysis.plugin.mpp.bean.proposer.points;

/*-
 * #%L
 * anchor-plugin-mpp
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import org.anchoranalysis.anchor.mpp.bean.bound.BoundUnitless;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;

// TODO do we still need the commented out code?
class ForceMinimumSizeHelper {

	private ForceMinimumSizeHelper() {}
	
	public static List<Point3i> forceMinimumZSize( List<Point3i> pntsIn, BoundUnitless bound, int forceMinZSize ) throws OperationFailedException {
		
		List<Point3i> pntsOut = new ArrayList<Point3i>();
		pntsOut.addAll(pntsIn);
		
		// How many extra do we need
		int boundSize = (int) Math.floor(bound.size());
		int boundMin = (int) Math.floor(bound.getMin());
		int boundMax = (int) Math.floor(bound.getMax());
		
		int extraNeeded = forceMinZSize - boundSize;
		
		int extraNeededLower = extraNeeded / 2;
		int extraNeededUpper = extraNeeded - extraNeededLower;
		
		int boundMinNew = boundMin - extraNeededLower;
		int boundMaxNew = boundMax + extraNeededUpper;
		
		
//		// We calculate a mean point
//		for( Point3i p : pntsIn) {
//			if (p.z==boundMin) {
//				pntsOut.add( new Point3i(p.x, p.y, z) );
//			}
//		}
//		
		
		
		Point3i cog = calcCOG(pntsIn);
		
//		double scale = 2;
//		
//		for( Point3i p : pntsIn) {
//
//			int zFromCog = p.z - cog.getZ();
//				
//			pntsOut.add( new Point3i(p.x, p.y, cog.getZ() + (int) (scale*zFromCog)) );
//			
//		}
		
		
		if (extraNeededLower!=0) {
			pntsOut.add( new Point3i(cog.getX(),cog.getY(),boundMinNew) );
		}
		
		if (extraNeededUpper!=0) {
			pntsOut.add( new Point3i(cog.getX(),cog.getY(),boundMaxNew) );
		}
		
		
//		for( int z=(boundMin-1); z>=boundMinNew; z--) {
//			// We add all the points with a new z
//			for( Point3i p : pntsIn) {
//				if (p.z==boundMin) {
//					
//					pntsOut.add( new Point3i(p.x, p.y, z) );
//				}
//			}
//		//pntsOut.add( new Point3i(cog.x,cog.y,z) );
//		}
		
		
//		for( int z=(boundMin-1); z>=boundMinNew; z--) {
//			// We add all the points with a new z
//			for( Point3i p : pntsIn) {
//				if (p.z==boundMin) {
//					
//					pntsOut.add( new Point3i(p.x, p.y, z) );
//				}
//			}
//			//pntsOut.add( new Point3i(cog.x,cog.y,z) );
//		}
//		
//		for( int z=(boundMin+1); z<=boundMaxNew; z++) {
//			// We add all the points with a new z
//			for( Point3i p : pntsIn) {
//				if (p.z==boundMax) {
//					pntsOut.add( new Point3i(p.x, p.y, z) );
//				}
//			}
//			//pntsOut.add( new Point3i(cog.x,cog.y,z) );
//		}		
		
		return pntsOut;
	}
	
	private static Point3i calcCOG( List<Point3i> pnts ) throws OperationFailedException {
		
		if (pnts.size()==0) {
			throw new OperationFailedException("There are no points in the list, so now center-of-gravity exists");
		}
		
		double sumX = 0.0;
		double sumY = 0.0;
		double sumZ = 0.0;
		int cnt= 0;
		for( Point3i p : pnts) {
			sumX += p.getX();
			sumY += p.getY();
			sumZ += p.getZ();
			cnt++;
		}
		
		int cogX = (int) Math.round(sumX/cnt);
		int cogY = (int) Math.round(sumY/cnt);
		int cogZ = (int) Math.round(sumZ/cnt);
		
		return new Point3i( cogX, cogY, cogZ  );
	}
}
