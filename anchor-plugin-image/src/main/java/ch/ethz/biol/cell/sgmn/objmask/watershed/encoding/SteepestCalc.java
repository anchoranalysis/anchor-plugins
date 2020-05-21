package ch.ethz.biol.cell.sgmn.objmask.watershed.encoding;

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
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.image.voxel.iterator.changed.InitializableProcessChangedPoint;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessChangedPointAbsolute;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessChangedPointFactory;
import org.anchoranalysis.image.voxel.nghb.BigNghb;
import org.anchoranalysis.image.voxel.nghb.Nghb;
import org.anchoranalysis.image.voxel.nghb.SmallNghb;

public final class SteepestCalc {

	private final boolean do3D;
	private final PointTester pt;
	private final InitializableProcessChangedPoint process;
	private final Nghb nghb;
	
	/**
	 * 
	 * @param rbb
	 * @param encoder
	 * @param do3D
	 * @param bigNghb iff true we use 8-Connectivity instead of 4, and 26-connectivity instead of 6 in 3D
	 * @param mask
	 */
	public SteepestCalc( SlidingBuffer<?> rbb, WatershedEncoding encoder, boolean do3D, boolean bigNghb, Optional<ObjMask> mask ) {
		this.do3D = do3D;
		this.pt = new PointTester(encoder,rbb);
		this.process = ProcessChangedPointFactory.within(mask, rbb.extnt(), pt);
		this.nghb = bigNghb ? new BigNghb() : new SmallNghb();
	}
	
	private static class PointTester implements ProcessChangedPointAbsolute {

		
		private SlidingBuffer<?> rbb;
		private WatershedEncoding encoder;
		
		private int centreVal;
		private int indxBuffer;
		
		private int steepestDrctn;
		private int steepestVal;
		
		
		private VoxelBuffer<?> bb;
		private int zChange;
		
		private Extent extnt;
		
		public PointTester(WatershedEncoding encoder, SlidingBuffer<?> rbb ) {
			this.encoder = encoder;
			this.rbb = rbb;
			this.extnt = rbb.extnt();
		}

		public void initPnt( int pnteVal, int indxBuffer ) {
			this.centreVal = pnteVal;
			this.indxBuffer = indxBuffer;
			this.steepestVal = pnteVal;
			this.steepestDrctn = WatershedEncoding.CODE_MINIMA;
		}


		@Override
		public boolean processPoint( int xChange, int yChange, int x1, int y1) {
			
			int indxChange = extnt.offset(xChange, yChange);
			int gValNghb = bb.getInt( indxBuffer + indxChange );
			
			// TODO check if it's okay these values exist?
			//assert( gValNghb!= WatershedEncoding.CODE_UNVISITED );
			//assert( gValNghb!= WatershedEncoding.CODE_TEMPORARY );
			
			if (gValNghb==centreVal) {
				steepestDrctn = WatershedEncoding.CODE_PLATEAU;
				return true;
			}
			
			if (gValNghb<steepestVal) {
				steepestVal = gValNghb;
				steepestDrctn = encoder.encodeDirection(xChange, yChange, zChange);
				return true;
			}
			
			return false;			
		}

		public int getSteepestDrctn() {
			return steepestDrctn;
		}

		@Override
		public void notifyChangeZ(int zChange, int z1) {
			this.bb = rbb.bufferRel(zChange);
			this.zChange = zChange;
		}
	}
	
	// Calculates the steepest descent
	public int calcSteepestDescent( Point3i pnt, int val, int indxBuffer ) {
		this.pt.initPnt(val, indxBuffer);
		IterateVoxels.callEachPointInNghb(pnt, nghb, do3D, process);
		return pt.getSteepestDrctn();
	}
}