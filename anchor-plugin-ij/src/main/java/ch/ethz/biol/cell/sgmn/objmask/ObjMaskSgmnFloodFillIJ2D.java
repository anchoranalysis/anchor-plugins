package ch.ethz.biol.cell.sgmn.objmask;

/*
 * #%L
 * anchor-plugin-ij
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


import ij.process.ImageProcessor;

import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmn;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;

import ch.ethz.biol.cell.imageprocessing.io.objmask.FloodFillUtils;

public class ObjMaskSgmnFloodFillIJ2D extends ObjMaskSgmn {
	
	// START BEAN PROPERTIES
	@BeanField
	private int minBoundingBoxVolumeVoxels = 1;
	
	@BeanField
	private int startingColor = 1;
	
	@BeanField
	private boolean doInt = false;		// Floodfills on an IntBuffer instead of a ByteBuffer
	// END BEAN PROPERTIES
	
	@Override
	public ObjMaskCollection sgmn(Chnl chnl,
			Optional<SeedCollection> seeds) throws SgmnFailedException {
		
		BinaryValuesByte bv = BinaryValuesByte.getDefault();
		try {

			ImageProcessor ip = IJWrap.imageProcessorByte(chnl.getVoxelBox().asByte().getPlaneAccess(), 0);
			int numC = FloodFillUtils.floodFill2D( ip, bv.getOnByte(), startingColor, minBoundingBoxVolumeVoxels );
	
			//log.debug("Creation-object-masks started");
			return ObjMaskChnlUtilities.calcObjMaskFromLabelChnl(
				chnl.getVoxelBox().asByte(),
				numC,
				minBoundingBoxVolumeVoxels
			);
			
		} catch (OperationFailedException e) {
			throw new SgmnFailedException(e);
		}
	}
	
	@Override
	public ObjMaskCollection sgmn(Chnl chnl, ObjMask objMask,
			Optional<SeedCollection> seeds) throws SgmnFailedException {
		throw new SgmnFailedException("Unsupported operation");
	}

	public int getMinBoundingBoxVolumeVoxels() {
		return minBoundingBoxVolumeVoxels;
	}


	public void setMinBoundingBoxVolumeVoxels(int minBoundingBoxVolumeVoxels) {
		this.minBoundingBoxVolumeVoxels = minBoundingBoxVolumeVoxels;
	}

	public int getStartingColor() {
		return startingColor;
	}

	public void setStartingColor(int startingColor) {
		this.startingColor = startingColor;
	}

	public boolean isDoInt() {
		return doInt;
	}

	public void setDoInt(boolean doInt) {
		this.doInt = doInt;
	}

}
