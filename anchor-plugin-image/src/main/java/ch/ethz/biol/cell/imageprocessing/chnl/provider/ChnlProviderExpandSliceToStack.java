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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

// Takes a 2-dimensional mask and converts into a 3-dimensional mask by repeating along the z-stack
public class ChnlProviderExpandSliceToStack extends ChnlProvider {

	// START BEAN PROPERTIES
	@BeanField
	private ImageDimProvider dimProviderTarget;
	
	@BeanField
	private ChnlProvider chnlProviderSlice;
	// END BEAN PROPERTIES
	
	@Override
	public Chnl create() throws CreateException {
		
		ImageDim sdTarget = dimProviderTarget.create();
		
		Chnl chnl = chnlProviderSlice.create();
		
		ImageDim sdSrc = chnl.getDimensions();
		
		if (sdSrc.getX()!=sdTarget.getX()) {
			throw new CreateException("x dimension is not equal");
		}
		if (sdSrc.getY()!=sdTarget.getY()) {
			throw new CreateException("y dimension is not equal");
		}
		
		Chnl chnlOut = ChnlFactory.instance().createEmptyUninitialised(sdTarget, VoxelDataTypeUnsignedByte.instance);
		
		VoxelBox<ByteBuffer> vbSlice = chnl.getVoxelBox().asByte(); 
		VoxelBox<ByteBuffer> vbOut = chnlOut.getVoxelBox().asByte();
		
		for( int z=0; z<chnlOut.getDimensions().getZ(); z++) {
			VoxelBuffer<ByteBuffer> bb = vbSlice.duplicate().getPixelsForPlane(0);
			vbOut.setPixelsForPlane(z, bb );
		}
		
		return chnlOut;
	}

	public ChnlProvider getChnlProviderSlice() {
		return chnlProviderSlice;
	}

	public void setChnlProviderSlice(ChnlProvider chnlProviderSlice) {
		this.chnlProviderSlice = chnlProviderSlice;
	}

	public ImageDimProvider getDimProviderTarget() {
		return dimProviderTarget;
	}

	public void setDimProviderTarget(ImageDimProvider dimProviderTarget) {
		this.dimProviderTarget = dimProviderTarget;
	}
}
