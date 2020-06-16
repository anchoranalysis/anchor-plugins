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
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectMaskCollection;
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
	public ObjectMaskCollection sgmn(
		Channel chnl,
		Optional<ObjectMask> mask,
		Optional<SeedCollection> seeds
	) throws SgmnFailedException {
		
		if (mask.isPresent()) {
			throw new SgmnFailedException("A mask is not supported for this operation");
		}
		
		if (seeds.isPresent()) {
			throw new SgmnFailedException("Seeds are not supported for this operation");
		}
		
		try {
			int numC = floodFillChnl(chnl);
			return objectsFromLabels(chnl, numC);
			
		} catch (OperationFailedException e) {
			throw new SgmnFailedException(e);
		}
	}
	
	/** 
	 * Flood fills a channel, converting it into objects each labelled with an incrementing integer id
	 * 
	 * @param chnl channel to flood-fill
	 * @return the number of objects (so that the label ids are 1.... N) 
	 * @throws OperationFailedException 
	 **/
	private int floodFillChnl( Channel chnl ) throws OperationFailedException {
		BinaryValuesByte bv = BinaryValuesByte.getDefault();
		ImageProcessor ip = IJWrap.imageProcessorByte(chnl.getVoxelBox().asByte().getPlaneAccess(), 0);
		return FloodFillUtils.floodFill2D( ip, bv.getOnByte(), startingColor, minBoundingBoxVolumeVoxels );
	}
	
	/**
	 * Create object-masks from an image labelled as per {@link floodFillChnl}
	 * 
	 * @param chnl a channel labelled as per {@link floodFillChnl}
	 * @param numLabels the number of objects, so that the label ids are a sequence (1,numLabels) inclusive. 
	 * @return a derived collection of objs
	 */
	private ObjectMaskCollection objectsFromLabels( Channel chnl, int numLabels ) {
		return CreateFromLabels.create(
			chnl.getVoxelBox().asByte(),
			1,
			numLabels,
			minBoundingBoxVolumeVoxels
		);
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
