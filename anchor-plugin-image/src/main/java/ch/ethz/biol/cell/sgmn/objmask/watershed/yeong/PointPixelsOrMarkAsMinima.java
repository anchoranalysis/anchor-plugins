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

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;

import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedIntBuffer;
import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedVoxelBox;
import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.SteepestCalc;

class PointPixelsOrMarkAsMinima {
	
	// MinimaStore is optional. Set to null if not desired
	public void doForMask( VoxelBox<?> vbImg, EncodedVoxelBox matS, ObjMask om, MinimaStore minimaStore ) {
		
		Extent e = vbImg.extnt();
		
		SlidingBuffer<?> rbb = new SlidingBuffer<>( vbImg );

		
		boolean do3D = e.getZ()>1;
		
		FindEqualVoxels findEqualVoxels = new FindEqualVoxels( vbImg, matS, do3D, om );
		SteepestCalc steepestCalc = new SteepestCalc(rbb,matS.getEncoding(), do3D ,true, om);
		
		Point3i crnrMin = om.getBoundingBox().getCrnrMin();
	
		byte maskOn = om.getBinaryValuesByte().getOnByte();
		
		Extent eObjMask = om.getVoxelBox().extnt();
		
		rbb.init(crnrMin.getZ());

		
		for (int z=0; z<eObjMask.getZ(); z++) {
		
			int z1 = z+crnrMin.getZ();
			EncodedIntBuffer bbS = matS.getPixelsForPlane(z1);
			
			ByteBuffer bbOM = om.getVoxelBox().getPixelsForPlane(z).buffer();
	
			for (int y=0; y<eObjMask.getY(); y++) {
				for (int x=0; x<eObjMask.getX(); x++) {
			
					int offsetObjMask = eObjMask.offset(x, y);

					if (bbOM.get(offsetObjMask)==maskOn) {

						int x1 = x+crnrMin.getX();
						int y1 = y+crnrMin.getY();
						
						int indxBuffer = e.offset(x1, y1);
						
						if (bbS.isUnvisited(indxBuffer)) {
							visitPixelMask( indxBuffer, x1, y1, z1, steepestCalc, bbS, matS, rbb, findEqualVoxels, do3D, om, minimaStore );
						}
					}
				}
			}
			
			// FIRST STEP
			
			
			rbb.shift();
		}
	}

	
	public void doForAll( VoxelBoxWrapper vbImg, EncodedVoxelBox matS, MinimaStore minimaStore) {
		
		Extent e = vbImg.any().extnt();
		
		SlidingBuffer<?> rbb = new SlidingBuffer<>( vbImg.any() );
		rbb.init();
		
		boolean do3D = e.getZ()>1;
		
		FindEqualVoxels findEqualVoxels = new FindEqualVoxels( vbImg.any(), matS, do3D );
		SteepestCalc steepestCalc = new SteepestCalc(rbb,matS.getEncoding(), do3D ,true);
		
		for (int z=0; z<e.getZ(); z++) {
		
			// FIRST STEP
			
			EncodedIntBuffer bbS = matS.getPixelsForPlane(z);

			int indxBuffer = 0;
			
			for (int y=0; y<e.getY(); y++) {
				for (int x=0; x<e.getX(); x++) {
					
					if (bbS.isUnvisited(indxBuffer)) {
						visitPixel( indxBuffer, x, y, z, steepestCalc, bbS, matS, rbb, findEqualVoxels, do3D, minimaStore );
					}

					indxBuffer++;
				}
			}
			
			rbb.shift();
		}
	}
	
	
	
	private void visitPixel( int indxBuffer, int x, int y, int z, SteepestCalc steepestCalc,
		EncodedIntBuffer bbS, EncodedVoxelBox matS, SlidingBuffer<?> rbb, FindEqualVoxels findEqualVoxels, boolean do3D, MinimaStore minimaStore )
	{
		// We get the value of g
		int gVal = rbb.getCentre().getInt(indxBuffer);
		
		// Calculate steepest descent. -1 indicates that there is no steepest descent
		int chainCode = steepestCalc.calcSteepestDescent(x,y,z,gVal,indxBuffer);
		
		if (matS.isMinima(chainCode)) {
			// Treat as local minima
			bbS.putCode(indxBuffer, chainCode);	
			
			if (minimaStore!=null) {
				minimaStore.add( new Point3i(x,y,z) );
			}
			
		} else if (matS.isPlateau(chainCode)) {
			EqualVoxelsPlateau plateau = findEqualVoxels.createPlateau(x,y,z);
			new MakePlateauLowerComplete(plateau, do3D).makeBufferLowerCompleteForPlateau(matS, minimaStore);
			
			
		} else {
			// Record steepest
			bbS.putCode(indxBuffer,chainCode);
		}
	}
	
	private void visitPixelMask( int indxBuffer, int x, int y, int z, SteepestCalc steepestCalc,
		EncodedIntBuffer bbS, EncodedVoxelBox matS, SlidingBuffer<?> rbb, FindEqualVoxels findEqualVoxels, boolean do3D, ObjMask om, MinimaStore minimaStore )
	{
		// We get the value of g
		int gVal = rbb.getCentre().getInt(indxBuffer);
		
		// Calculate steepest descent. -1 indicates that there is no steepest descent

		int chainCode = steepestCalc.calcSteepestDescent(x,y,z,gVal,indxBuffer);
		//int chainCode2 = steepestCalc2.calcSteepestDescent(x,y,z,gVal,indxBuffer);
		
		//assert( chainCode==chainCode2 );
		
		if (matS.isMinima(chainCode)) {
			// Treat as local minima
			bbS.putCode(indxBuffer, chainCode);
			
			if (minimaStore!=null) {
				minimaStore.add( new Point3i(x,y,z) );
			}
			
		} else if (matS.isPlateau(chainCode)) {
			// This should also be contrainted by the object mask   UNDER UNDER CHANGE
			EqualVoxelsPlateau plateau = findEqualVoxels.createPlateau(x,y,z);
			new MakePlateauLowerComplete(plateau, do3D).makeBufferLowerCompleteForPlateau(matS, minimaStore);
			
		} else {
			// Record steepest
			bbS.putCode(indxBuffer,chainCode);
		}
	}
}