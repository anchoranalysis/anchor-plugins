package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

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
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessChangedPointAbsoluteMasked;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighbour;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighbourFactory;
import org.anchoranalysis.image.voxel.nghb.BigNghb;
import org.anchoranalysis.image.voxel.nghb.Nghb;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedVoxelBox;

class MakePlateauLowerComplete {

	private static class PointTester implements ProcessChangedPointAbsoluteMasked<List<Point3i>> {

		// STATIC
		private EncodedVoxelBox matS;
		
		private final List<Point3i> foundPoints = new ArrayList<>();	
		
		private int zChange;
		private ByteBuffer bb;
		private int z1;
		private final byte maskValueOff;
		
		public PointTester(EncodedVoxelBox matS, BinaryValuesByte bv) {
			super();
			this.matS = matS;
			this.maskValueOff = bv.getOffByte();
		}
		
		@Override
		public void initSource(int sourceVal, int sourceOffsetXY) {
			foundPoints.clear();	
		}

		@Override
		public void notifyChangeZ(int zChange, int z1,
				ByteBuffer objectMaskBuffer) {
			this.bb = objectMaskBuffer;
			this.zChange = zChange;
			this.z1 = z1;
		}

		@Override
		public boolean processPoint(int xChange, int yChange, int x1, int y1, int objectMaskOffset) {
			
			Point3i pointRel = new Point3i(x1,y1,z1);
			foundPoints.add(pointRel);
			bb.put(objectMaskOffset,maskValueOff);
			
			// We point this value in the direction opposite to which it came
			matS.setPointDirection(pointRel, xChange*-1,yChange*-1,zChange*-1);
			
			return true;
		}

		@Override
		public List<Point3i> collectResult() {
			return foundPoints;
		}
	}
	
	private EqualVoxelsPlateau plateau;
	private boolean do3D;

	public MakePlateauLowerComplete(EqualVoxelsPlateau plateau, boolean do3D ) {
		super();
		this.plateau = plateau;
		this.do3D = do3D;
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
			
			matS.pointListAtFirstPoint( plateau.getPtsInner() );
			
			if (minimaStore.isPresent()) {
				minimaStore.get().add( plateau.getPtsInner() );
			}
		} else {
			
			// IF IT'S MIXED...
			
			pointEdgeToNeighbouring( matS );
			pointInnerToEdge( matS );
		}
	}
	
	private void pointEdgeToNeighbouring( EncodedVoxelBox matS ) {
		// We set them all to their neighbouring points
		for( PointWithNghb pointNghb : plateau.getPtsEdge()) {
			assert( pointNghb.getNghbIndex() >= 0 );
			
			// IMPROVE BY SORTING BY Z-VALUE
			//ByteBuffer bb = rbb.bufferRel( pointNghb.getPoint().getZ() )
			matS.setPoint( pointNghb.getPoint(), pointNghb.getNghbIndex() );
		}		
	}
		
	private void pointInnerToEdge( EncodedVoxelBox matS ) {
		// Iterate through each edge pixel, and look for neighbouring points in the Inner pixels
		//   for any such point, point towards the edge pixel, and move to the new edge list
		List<Point3i> searchPoints = plateau.ptsEdgeAsPoints();
		
		try {
			// We create an object-mask from the list of points
			ObjectMask object = CreateObjectFromPoints.create( plateau.getPtsInner() );
			Nghb nghb = new BigNghb();

			ProcessVoxelNeighbour<List<Point3i>> process = ProcessVoxelNeighbourFactory.withinMask(
				object,
				new PointTester(matS, object.getBinaryValuesByte())
			);
			
			while( !searchPoints.isEmpty() ) {
				searchPoints = findPointsFor(searchPoints, nghb, process);
			}
			
		} catch (CreateException e) {
			// the only exception possible should be when there are 0 pixels
			assert false;
		}
	}
	
	private List<Point3i> findPointsFor( List<Point3i> points, Nghb nghb, ProcessVoxelNeighbour<List<Point3i>> process ) {
		
		List<Point3i> foundPoints = new ArrayList<>();
		
		// We iterate through all the search points
		for( Point3i p : points ) {
			
			foundPoints.addAll(
				IterateVoxels.callEachPointInNghb(
					p,
					nghb,
					do3D,
					process,
					-1,	// The -1 value are arbitrary, as it will be ignored
					-1  // The -1 value are arbitrary, as it will be ignored
				)
			);
		}
		return foundPoints;
	}
}
