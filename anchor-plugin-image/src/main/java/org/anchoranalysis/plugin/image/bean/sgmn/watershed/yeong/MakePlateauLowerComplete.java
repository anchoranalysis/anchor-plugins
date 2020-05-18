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


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.nghb.BigNghb;
import org.anchoranalysis.image.voxel.nghb.IProcessAbsolutePointObjectMask;
import org.anchoranalysis.image.voxel.nghb.Nghb;
import org.anchoranalysis.image.voxel.nghb.iterator.PointObjMaskIterator;

import ch.ethz.biol.cell.sgmn.objmask.ObjMaskChnlUtilities;
import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedVoxelBox;

class MakePlateauLowerComplete {

	private EqualVoxelsPlateau plateau;
	private boolean do3D;

	public MakePlateauLowerComplete(EqualVoxelsPlateau plateau, boolean do3D ) {
		super();
		this.plateau = plateau;
		this.do3D = do3D;
	}

	private void pointEdgeToNeighbouring( EncodedVoxelBox matS ) {
		// We set them all to their neighbouring points
		for( PointWithNghb pntNghb : plateau.getPtsEdge()) {
			assert( pntNghb.getNghbIndex() >= 0 );
			
			// IMPROVE BY SORTING BY Z-VALUE
			//ByteBuffer bb = rbb.bufferRel( pntNghb.getPnt().getZ() )
			matS.setPoint( pntNghb.getPnt(), pntNghb.getNghbIndex() );
		}		
	}
	
	
	private static class PointTester implements IProcessAbsolutePointObjectMask {

		// STATIC
		private EncodedVoxelBox matS;
		
		private List<Point3i> foundPoints;
		
		private int zChange;
		private ByteBuffer bb;
		private int z1;
		private final byte maskValueOff;
		
		public PointTester(EncodedVoxelBox matS, BinaryValuesByte bv) {
			super();
			this.matS = matS;
			this.maskValueOff = bv.getOffByte();
		}
		
		public void resetFoundPoints() {
			this.foundPoints = new ArrayList<>();			
		}
		
		public boolean processPoint(int xChange, int yChange, int x1, int y1, int objectMaskOffset) {
			
			Point3i pntRel = new Point3i(x1,y1,z1);
			foundPoints.add(pntRel);
			bb.put(objectMaskOffset,maskValueOff);
			
			// We point this value in the direction opposite to which it came
			matS.setPointDirection(pntRel, xChange*-1,yChange*-1,zChange*-1);
			
			return true;
		}

		public List<Point3i> getFoundPoints() {
			return foundPoints;
		}

		@Override
		public void notifyChangeZ(int zChange, int z1,
				ByteBuffer objectMaskBuffer) {
			this.bb = objectMaskBuffer;
			this.zChange = zChange;
			this.z1 = z1;
		}
	}
	
	
	private void pointInnerToEdge( EncodedVoxelBox matS ) {
		
		// Iterate through each edge pixel, and look for neighbouring points in the Inner pixels
		//   for any such point, point towards the edge pixel, and move to the new edge list
		
		
		
		List<Point3i> searchPoints = plateau.ptsEdgeAsPoints();
		
		try {
			// We create an objMask from the list of points
			ObjMask om = ObjMaskChnlUtilities.createObjMaskFromPoints( plateau.getPtsInner() );
			Nghb nghb = new BigNghb();
			
			
			PointTester pt = new PointTester(matS, om.getBinaryValuesByte());
			PointObjMaskIterator itr = new PointObjMaskIterator(pt, om);
			
			while( !searchPoints.isEmpty() ) {
				
				pt.resetFoundPoints();
				
				// We iterate through all the search points
				for( Point3i p : searchPoints ) {
					itr.initPnt(p.getX(), p.getY(), p.getZ());
					nghb.processAllPointsInNghb(do3D, itr);
				}
				searchPoints = pt.getFoundPoints();
			}
			
		} catch (CreateException e) {
			// the only exception possible should be when there are 0 pixels
			assert false;
		}
	}
	
	public void makeBufferLowerCompleteForPlateau( EncodedVoxelBox matS, Optional<MinimaStore> minimaStore ) {
		
		assert(plateau.hasPoints());
		if (plateau.isOnlyEdge()) {
			pointEdgeToNeighbouring( matS );
		} else if (plateau.isOnlyInner()) {
			// EVERYTHING COLLECTIVELY IS A LOCAL MINIMA
			// We pick one pixel to use as an index, and pointing the rest of the pixels
			//  of them towards it
			
			assert plateau.getPtsInner().size() >= 2;
			
			matS.pointListAtFirst( plateau.getPtsInner() );
			
			if (minimaStore.isPresent()) {
				minimaStore.get().add( plateau.getPtsInner() );
			}
		} else {
			
			// IF IT'S MIXED...
			
			pointEdgeToNeighbouring( matS );
			pointInnerToEdge( matS );
		}
	}
}
