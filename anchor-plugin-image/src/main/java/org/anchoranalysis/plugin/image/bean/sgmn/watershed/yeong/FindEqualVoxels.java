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


import java.util.Optional;
import java.util.Stack;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighbour;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighbourAbsoluteWithSlidingBuffer;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighbourFactory;
import org.anchoranalysis.image.voxel.nghb.BigNghb;

import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedIntBuffer;
import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedVoxelBox;

final class FindEqualVoxels {
	
private static class PointTester extends ProcessVoxelNeighbourAbsoluteWithSlidingBuffer<Optional<Integer>> {
		
		// Static arguments
		private Stack<Point3i> stack;
		
		private EncodedVoxelBox matS;
		
		// Arguments for each point
		private int lowestNghbVal;
		private int lowestNghbIndex = -1;
				
		private EncodedIntBuffer bufS;
		private int z1;
		
		public PointTester(Stack<Point3i> stack, SlidingBuffer<?> rbb, EncodedVoxelBox matS) {
			super(rbb);
			this.stack = stack;
			this.matS = matS;
		}

		@Override
		public void initSource( int sourceVal, int sourceOffsetXY) {
			super.initSource(sourceVal, sourceOffsetXY);
			this.lowestNghbVal = sourceVal;
			this.lowestNghbIndex = - 1;
		}

		/** The lowestNghbIndex if it exists */
		@Override
		public Optional<Integer> collectResult() {
			if (lowestNghbIndex!=-1) {
				return Optional.of(lowestNghbIndex);
			} else {
				return Optional.empty();
			}
		}
		
		@Override
		public void notifyChangeZ(int zChange, int z1) {
			super.notifyChangeZ(zChange, z1);
			bufS = matS.getPixelsForPlane(z1);
			this.z1 = z1;
		}

		@Override
		public boolean processPoint(int xChange, int yChange, int x1, int y1) {

			int offset = changedOffset(xChange,yChange);
			int valPoint = getInt(offset);
			
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
			 
			if (valPoint==sourceVal) {
				stack.push( new Point3i(x1, y1, z1) );
				return true;
			} else {
				// We test if the neighbour is less
				// NB we also force a check that it's less than the value to find, as this value
				//   might have been forced up by the connected component
				if (valPoint<sourceVal && valPoint<lowestNghbVal) {
					lowestNghbVal = valPoint;
					//lowestNghbIndex = extent.offset(x1, y1, z1);
					lowestNghbIndex = matS.getEncoding().encodeDirection(xChange, yChange, zChange);
				}
				return false;
			}
		}
	}

	private final VoxelBox<?> bufferValuesToFindEqual;
	private final EncodedVoxelBox matS;
	private final boolean do3D;
	private final Optional<ObjMask> mask;
	
	public FindEqualVoxels(
		VoxelBox<?> bufferValuesToFindEqual,
		EncodedVoxelBox temporaryMarkVisitedBuffer,
		boolean do3D,
		Optional<ObjMask> mask
	) {
		this.bufferValuesToFindEqual = bufferValuesToFindEqual;
		this.matS = temporaryMarkVisitedBuffer;
		this.do3D = do3D;
		this.mask = mask;
	}
	
	public EqualVoxelsPlateau createPlateau(Point3i pnt) {
		
		EqualVoxelsPlateau plateau = new EqualVoxelsPlateau();
				
		int valToFind = 
			bufferValuesToFindEqual.getPixelsForPlane( pnt.getZ() ).getInt(
				bufferValuesToFindEqual.extent().offsetSlice(pnt)
			);
		
		SlidingBuffer<?> rbb = new SlidingBuffer<>(bufferValuesToFindEqual);
		
		{
			Stack<Point3i> stack = new Stack<>(); 
			stack.push(pnt);
			processStack(stack, rbb, plateau, valToFind);
		}
		
		assert( !plateau.hasNullItems() );
		
		return plateau;
	}
	
	private void processStack( Stack<Point3i> stack, SlidingBuffer<?> slidingBuffer, EqualVoxelsPlateau plateau, int valToFind ) {
				
		ProcessVoxelNeighbour<Optional<Integer>> process = ProcessVoxelNeighbourFactory.within(
			mask,
			slidingBuffer.extent(),
			new PointTester(stack, slidingBuffer, matS)
		);
				
		BigNghb nghb = new BigNghb();
		
		while( !stack.isEmpty() ) {
			Point3i pnt = stack.pop();
			
			// If we've already visited this point, we skip it
			EncodedIntBuffer bbVisited = matS.getPixelsForPlane( pnt.getZ() );
			int offset = slidingBuffer.extent().offsetSlice(pnt);
			if (bbVisited.isTemporary(offset)) {
				continue;
			}
			
			slidingBuffer.seek(pnt.getZ());

			Optional<Integer> lowestNghbIndex = IterateVoxels.callEachPointInNghb(pnt, nghb, do3D, process, valToFind, offset);
			
			bbVisited.markAsTemporary(offset);
						
			if (lowestNghbIndex.isPresent()) {
				plateau.addEdge(pnt, lowestNghbIndex.get());
			} else {
				plateau.addInner(pnt);				
			}
		}
	}
	
	public boolean isDo3D() {
		return do3D;
	}
}