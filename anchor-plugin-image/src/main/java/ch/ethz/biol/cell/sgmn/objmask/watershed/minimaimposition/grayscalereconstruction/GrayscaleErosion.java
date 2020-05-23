package ch.ethz.biol.cell.sgmn.objmask.watershed.minimaimposition.grayscalereconstruction;

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

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighbour;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighbourAbsoluteWithSlidingBuffer;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighbourFactory;
import org.anchoranalysis.image.voxel.nghb.BigNghb;
import org.anchoranalysis.image.voxel.nghb.Nghb;


// Performs grayscale erosion on a point using a 3x3 FLAT structuring element
// The same as MINIMUM filter
// NOT EFFICIENTLY IMPLEMENTED
public class GrayscaleErosion {

	private static class PointTester extends ProcessVoxelNeighbourAbsoluteWithSlidingBuffer<Integer> {
		
		// Current minima
		private int minima;
		
		public PointTester(SlidingBuffer<ByteBuffer> rbb) {
			super(rbb);
		}

		@Override
		public void initSource( int indx, int exstVal ) {
			super.initSource(exstVal, indx);
			minima = exstVal;
		}

		@Override
		public boolean processPoint(int xChange, int yChange, int x1, int y1) {
			int val = getInt(xChange, yChange);
			
			if (val < minima) {
				minima = val;
				return true;
			}
			return false;
		}

		@Override
		public Integer collectResult() {
			return minima;
		}
	}
	
	private static final Nghb NGHB = new BigNghb(false);
	
	private final boolean do3D;
	private final ProcessVoxelNeighbour<Integer> pointProcessor;
	
	// Without mask
	public GrayscaleErosion( SlidingBuffer<ByteBuffer> rbb, boolean do3D ) {
		this.do3D = do3D;
		this.pointProcessor = ProcessVoxelNeighbourFactory.withinExtent(
			new PointTester(rbb)
		);
	}
	
	// The sliding buffer must be centred at the current value of z
	public int grayscaleErosion( Point3i pnt, SlidingBuffer<ByteBuffer> buffer, int indx, int exstVal ) {
		return IterateVoxels.callEachPointInNghb(pnt, NGHB, do3D, pointProcessor, exstVal, indx);
	}
	
	// Returns a bool, if at least one pixel changed
	public static boolean grayscaleErosion( VoxelBox<ByteBuffer> vbIn, VoxelBox<ByteBuffer> vbOut ) {
				
		Extent e = vbIn.extnt();
		// We iterate

		SlidingBuffer<ByteBuffer> sb = new SlidingBuffer<>(vbIn);
		sb.init();
		
		GrayscaleErosion ge = new GrayscaleErosion(sb, vbIn.extnt().getZ() > 1 );
		
		boolean pixelChanged = false;
		
		for (int z=0; z<e.getZ(); z++) {
			
			ByteBuffer bbOut = vbOut.getPixelsForPlane(z).buffer();
			
			int indx = 0;
			for (int y=0; y<e.getY(); y++) {
				for (int x=0; x<e.getX(); x++) {

					
					int valExst = sb.getCentre().getInt(indx);
					
					int valNew = ge.grayscaleErosion(
						new Point3i(x, y, z),
						sb,
						indx,
						valExst
					);
					bbOut.put(indx, (byte) valNew );
					
					if (valNew!=valExst) {
						pixelChanged = true;
					}
					
					indx++;
				}
			}
			
			sb.shift();
		}
		
		return pixelChanged;
	}
}
