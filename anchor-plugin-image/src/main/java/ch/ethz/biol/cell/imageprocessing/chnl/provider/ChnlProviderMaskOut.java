package ch.ethz.biol.cell.imageprocessing.chnl.provider;

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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.voxel.box.VoxelBox;

/**
 * Set pixels NOT IN the mask to 0, but keep pixels IN the mask identical.
 * 
 * <p>It's an immutable operation and a new channel is always produced.</p>
 * 
 * @author Owen Feehan
 *
 */
public class ChnlProviderMaskOut extends ChnlProviderOneMask {
	
	@Override
	protected Chnl createFromMaskedChnl(Chnl chnl, BinaryChnl mask) throws CreateException {

		VoxelBox<ByteBuffer> vbMask = mask.getChnl().getVoxelBox().asByte();
		
		Chnl chnlOut = ChnlFactory.instance().createEmptyInitialised( chnl.getDimensions(), chnl.getVoxelDataType() );
				
		BoundingBox bbox = new BoundingBox( chnlOut.getDimensions().getExtnt() );
		chnl.getVoxelBox().copyPixelsToCheckMask(
			bbox,
			chnlOut.getVoxelBox(),
			bbox,
			vbMask,
			mask.getBinaryValues().createByte()
		);
		
		return chnlOut;
	}
}