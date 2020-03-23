package ch.ethz.biol.cell.imageprocessing.objmask.provider;

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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;

// Chops objects up into squares (or mostly squares, sometimes rectangles at the very end)
// Only chops in X and Y, Z is unaffected
public class ObjMaskProviderSplitIntoSquares extends ObjMaskProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objs;
	
	@BeanField @Positive
	private int squareSize = 10;
	
	@BeanField
	private int minNumVoxels = 1;
	// END BEAN PROPERTIES
	
	@Override
	public ObjMaskCollection create() throws CreateException {

		ObjMaskCollection objsCollection = objs.create();
		
		ObjMaskCollection out = new ObjMaskCollection();
		for (ObjMask om : objsCollection) {
			out.addAll(
				splitObj(
					om,
					squareSize,
					minNumVoxels
				)
			);
		}
		return out;
	}
	
	// We want to add in any remaining space at the end into the last object, so we never have a rectangle
	//  smaller than our squareSDize
	public static ObjMaskCollection splitObj( ObjMask om, int squareSize, int minNumVoxels ) {
		
		ObjMaskCollection out = new ObjMaskCollection();
		
		Extent e = om.getBoundingBox().extnt();
		
		int numX = e.getX() / squareSize;
		int numY = e.getY() / squareSize;
		
		// We force at least one, to catch the remainder
		if (numX==0) numX=1;
		if (numY==0) numY=1;
		
		for( int y=0; y<numY; y++ ) {
			
			int startY = y * squareSize;
			
			int endY = Math.min(startY+squareSize,e.getY());
			
			// Special treatment for last square
			if (y==(numY-1)) {
				endY = Math.min(endY+squareSize,e.getY());
			}
			
			int extntY = endY-startY;
			
			for( int x=0; x<numX; x++ ) {
			
				int startX = x * squareSize;
				int endX = Math.min(startX+squareSize,e.getX());
				
				// Special treatment for last square
				if (x==(numX-1)) {
					endX = Math.min(endX+squareSize,e.getX());
				}				
			
				int extntX = endX-startX;
				
				Extent extntNew = new Extent(extntX,extntY,om.getVoxelBox().extnt().getZ());
				BoundingBox srcBox = new BoundingBox(new Point3i(startX,startY,0), extntNew);
				
				VoxelBox<ByteBuffer> vbNew = VoxelBoxFactory.getByte().create(extntNew);
				
				om.getVoxelBox().copyPixelsTo(srcBox, vbNew, new BoundingBox(extntNew));
				
				// We only add the square if there's at least one voxel in it
				if (minNumVoxels==1) {
					if (!vbNew.hasEqualTo(om.getBinaryValues().getOnInt())) {
						continue;
					}
				} else {
					int cntOn = vbNew.countEqual(om.getBinaryValues().getOnInt());
					if (cntOn<minNumVoxels) {
						continue;
					}
				}
					
				// Now we add our box to the collection
				srcBox.getCrnrMin().add(om.getBoundingBox().getCrnrMin() );
				
				out.add( new ObjMask(srcBox, vbNew, om.getBinaryValuesByte()));
			}
		}
		
		return out;
	}

	public ObjMaskProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}

	public int getSquareSize() {
		return squareSize;
	}

	public void setSquareSize(int squareSize) {
		this.squareSize = squareSize;
	}

	public int getMinNumVoxels() {
		return minNumVoxels;
	}

	public void setMinNumVoxels(int minNumVoxels) {
		this.minNumVoxels = minNumVoxels;
	}



}
