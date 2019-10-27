package ch.ethz.biol.cell.sgmn.objmask.watershed.yeong;

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
import java.util.Stack;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.nghb.BigNghb;
import org.anchoranalysis.image.voxel.nghb.IProcessAbsolutePoint;
import org.anchoranalysis.image.voxel.nghb.IProcessAbsolutePointObjectMask;
import org.anchoranalysis.image.voxel.nghb.iterator.PointExtntIterator;
import org.anchoranalysis.image.voxel.nghb.iterator.PointIterator;
import org.anchoranalysis.image.voxel.nghb.iterator.PointObjMaskIterator;

import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedIntBuffer;
import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedVoxelBox;

class FindEqualVoxels {
	
	private VoxelBox<?> bufferValuesToFindEqual;
	private EncodedVoxelBox matS;
	private boolean do3D;
	private ObjMask objMask;
	
	public FindEqualVoxels(VoxelBox<?> bufferValuesToFindEqual,
			EncodedVoxelBox temporaryMarkVisitedBuffer,
			boolean do3D) {
		super();
		this.bufferValuesToFindEqual = bufferValuesToFindEqual;
		this.matS = temporaryMarkVisitedBuffer;
		this.do3D = do3D;
		this.objMask = null;
	}
	
	public FindEqualVoxels(VoxelBox<?> bufferValuesToFindEqual,
			EncodedVoxelBox matS,
			boolean do3D, ObjMask objMask) {
		super();
		this.bufferValuesToFindEqual = bufferValuesToFindEqual;
		this.matS = matS;
		this.do3D = do3D;
		this.objMask = objMask;
	}
	
	
	public EqualVoxelsPlateau createPlateau( int x, int y, int z ) {
		
		EqualVoxelsPlateau plateau = new EqualVoxelsPlateau();
				
		int valToFind = 
			bufferValuesToFindEqual.getPixelsForPlane(z).getInt(
				bufferValuesToFindEqual.extnt().offset(x, y)
			);
		
		{
			Stack<Point3i> stack = new Stack<>(); 
			stack.push( new Point3i(x,y,z) );
			processStack(stack, plateau, valToFind);
		}
		
		//assert( plateau.size() >= 2 );
		assert( !plateau.hasNullItems() );
		
		return plateau;
	}
	
	private void processStack( Stack<Point3i> stack, EqualVoxelsPlateau plateau, int valToFind ) {

		Extent extnt = bufferValuesToFindEqual.extnt();
		SlidingBuffer<?> rbb = new SlidingBuffer<>(bufferValuesToFindEqual);
		
		
		PointTester pt = new PointTester( stack, extnt, rbb, matS);
		
		
		PointIterator itr = objMask!= null ?  new PointObjMaskIterator(pt, objMask) : new PointExtntIterator(extnt, pt);
		
		BigNghb nghb = new BigNghb();
		
		while( !stack.isEmpty() ) {
			Point3i pnt = stack.pop();

			int x = pnt.getX();
			int y = pnt.getY();
			int z = pnt.getZ();
			
			// If we've already visited this point, we skip it
			EncodedIntBuffer bbVisited = matS.getPixelsForPlane( pnt.getZ() );
			int offset = extnt.offset(x, y);
			if (bbVisited.isTemporary(offset)) {
				continue;
			}
			
			rbb.init(z);

			itr.initPnt(pnt.getX(), pnt.getY(), pnt.getZ());
			pt.initForPoint(valToFind);

			nghb.processAllPointsInNghb(do3D, itr);
			
			bbVisited.markAsTemporary(offset);
			
			if (pt.hasLowestNghbIndex()) {
				plateau.addEdge(pnt, pt.getLowestNghbIndex());
			} else {
				plateau.addInner(pnt);				
			}
		}
	}
	
	
	
	private static class PointTester implements IProcessAbsolutePoint, IProcessAbsolutePointObjectMask {
		
		// Static arguments
		private Stack<Point3i> stack;
		private Extent extnt;
		private SlidingBuffer<?> rbb;
		private EncodedVoxelBox matS;
		
		// Arguments for each point
		private int lowestNghbVal;
		private int lowestNghbIndex = -1;
		private int valToFind;
		
		private VoxelBuffer<?> bb;
		private EncodedIntBuffer bufS;
		private int z1;
		private int zChange;
		
		public PointTester(Stack<Point3i> stack, Extent extnt,
				SlidingBuffer<?> rbb, EncodedVoxelBox matS) {
			super();
			this.stack = stack;
			this.extnt = extnt;
			this.rbb = rbb;
			this.matS = matS;
		}

		public void initForPoint( int pntVal ) {
			this.lowestNghbVal = pntVal;
			this.lowestNghbIndex = - 1;
			this.valToFind = pntVal;
		}
		
		private void pushStack( int x1, int y1, int z1 ) {
			stack.push( new Point3i(x1, y1, z1) );			
		}
		
		public boolean hasLowestNghbIndex() {
			return lowestNghbIndex!=-1;
		}

		public int getLowestNghbIndex() {
			return lowestNghbIndex;
		}

		@Override
		public void notifyChangeZ(int zChange, int z1) {
			bb = rbb.bufferRel(zChange);
			bufS = matS.getPixelsForPlane(z1);
			this.zChange = zChange;
			this.z1 = z1;
		}

		@Override
		public boolean processPoint(int xChange, int yChange, int x1,
				int y1) {
			
			int offset = extnt.offset( x1, y1 );
			
			int valPoint = bb.getInt(offset);
			
			// If we already have a connected component ID as a neighbour, it must because
			//   we have imposed seeds.  So we always point towards it, irrespective of
			//   its value
			if (bufS.isConnectedComponentID(offset)) {
				
				if (lowestNghbIndex==-1) {
					// We take anything
					lowestNghbVal = valPoint;
					lowestNghbIndex = matS.getEncoding().encodeDirection(xChange, yChange, zChange);
					return false;
				} else {
					if (valPoint<lowestNghbVal) {
						lowestNghbVal = valPoint;
						lowestNghbIndex = matS.getEncoding().encodeDirection(xChange, yChange, zChange);
					}
					return false;	
				}
				
				
			}
			
			 
			if (valPoint==valToFind) {
				//bbox.add( x1,y1,z1 );
				//bb.put(offset, CURRENT_OBJ);
				pushStack(x1,y1,z1);
				return true;
			} else {
				
				// We test if the neighbour is less
				// NB we also force a check that it's less than the value to find, as this value
				//   might have been forced up by the connected component
				if (valPoint<valToFind && valPoint < lowestNghbVal) {
					lowestNghbVal = valPoint;
					//lowestNghbIndex = extnt.offset(x1, y1, z1);
					lowestNghbIndex = matS.getEncoding().encodeDirection(xChange, yChange, zChange);
				}
				
				return false;
			}
		}

		@Override
		public void notifyChangeZ(int zChange, int z,
				ByteBuffer objectMaskBuffer) {
			notifyChangeZ(zChange, z);
		}

		@Override
		public boolean processPoint(int xChange, int yChange, int x1, int y1,
				int objectMaskOffset) {
			return processPoint(xChange, yChange, x1, y1);
		}
	}
}