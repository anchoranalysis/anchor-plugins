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

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.image.voxel.iterator.ProcessVoxel;

import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedIntBuffer;
import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedVoxelBox;

class PointPixelsOrMarkAsMinima {
	
	private PointPixelsOrMarkAsMinima() {}
		
	public static void apply( VoxelBox<?> vbImg, EncodedVoxelBox matS, Optional<ObjMask> mask, Optional<MinimaStore> minimaStore ) {
		
		SlidingBufferPlus buffer = new SlidingBufferPlus(vbImg, matS, mask, minimaStore);
		IterateVoxels.callEachPoint(
			buffer.getSlidingBuffer(),
			mask,
			new PointProcessor(buffer)
		);
	}
	
	private static final class PointProcessor implements ProcessVoxel {

		private final SlidingBufferPlus bufferPlus;
		
		private EncodedIntBuffer bbS;
		
		public PointProcessor(SlidingBufferPlus bufferPlus) {
			super();
			this.bufferPlus = bufferPlus;
		}
		
		@Override
		public void process(Point3i pnt) {
			
			int indxBuffer = bufferPlus.offsetSlice(pnt);
			
			// Exit early if this voxel has already been visited
			if (!bbS.isUnvisited(indxBuffer)) {
				return;
			}
			
			// We get the value of g
			int gVal = bufferPlus.getG(indxBuffer);
			
			// Calculate steepest descent. -1 indicates that there is no steepest descent
			int chainCode = bufferPlus.calcSteepestDescent(pnt,gVal,indxBuffer);
			
			if (bufferPlus.isMinima(chainCode)) {
				// Treat as local minima
				bbS.putCode(indxBuffer, chainCode);	
				bufferPlus.maybeAddMinima(pnt);
				
			} else if (bufferPlus.isPlateau(chainCode)) {
				bufferPlus.makePlateauAt(pnt);
			} else {
				// Record steepest
				bbS.putCode(indxBuffer,chainCode);
			}
		}

		@Override
		public void notifyChangeZ(int z) {
			bbS = bufferPlus.getSPlane(z);
		}
	}
}