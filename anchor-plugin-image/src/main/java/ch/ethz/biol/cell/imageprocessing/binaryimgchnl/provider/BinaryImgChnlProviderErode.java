package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.objmask.morph.MorphologicalErosion;

/**
 * Performs an erosion morphological operation on a binary-image
 */
public class BinaryImgChnlProviderErode extends BinaryImgChnlProviderMorphOp {

	// START
	@BeanField
	private boolean outsideAtThreshold = true;
	// END
	
	// Assumes imgChnlOut has the same ImgChnlRegions
	@Override
	protected void applyMorphOp( BinaryChnl imgChnl, boolean do3D ) throws CreateException{
		
		BinaryVoxelBox<ByteBuffer> out = MorphologicalErosion.erode(
			imgChnl.binaryVoxelBox(),
			do3D,
			getIterations(),
			backgroundVb(),
			getMinIntensityValue(),
			outsideAtThreshold,
			null
		);
		
		try {
			imgChnl.replaceBy(out);
		} catch (IncorrectImageSizeException e) {
			assert false;
		}
	}

	public boolean isOutsideAtThreshold() {
		return outsideAtThreshold;
	}


	public void setOutsideAtThreshold(boolean outsideAtThreshold) {
		this.outsideAtThreshold = outsideAtThreshold;
	}


}
