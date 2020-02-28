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
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

// Ors the receiveProvider onto the binaryImgChnlProvider
public class BinaryImgChnlProviderRepeatSlice extends BinaryImgChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private BinaryImgChnlProvider binaryImgChnlProvider;
	
	@BeanField
	private ImageDimProvider dimProvider;
	// END BEAN PROPERTIES

	@Override
	public BinaryChnl create() throws CreateException {
		
		BinaryChnl chnl = binaryImgChnlProvider.create();
		
		Chnl chnlIn = chnl.getChnl();
		VoxelBox<ByteBuffer> vbIn = chnlIn.getVoxelBox().asByte();
		
		ImageDim dims = dimProvider.create();
		
		if (chnl.getDimensions().getX()!=dims.getX() && chnl.getDimensions().getY()!=dims.getY() ) {
			throw new CreateException("dims do not match");
		}
		
		Chnl chnlOut = ChnlFactory.instance().createEmptyInitialised(dims, VoxelDataTypeUnsignedByte.instance);
		VoxelBox<ByteBuffer> vbOut = chnlOut.getVoxelBox().asByte();
		
		for( int z=0; z<chnlOut.getDimensions().getExtnt().getZ(); z++) {

			ByteBuffer bbOut = vbOut.getPixelsForPlane(z).buffer();
			
			ByteBuffer bbIn = vbIn.getPixelsForPlane(0).buffer();
			
			int vol = vbIn.extnt().getVolumeXY();
			for( int i=0; i<vol; i++ ) {
				bbOut.put(i, bbIn.get(i) );
			}
		}
		
		return new BinaryChnl( chnlOut, chnl.getBinaryValues() );
	}

	public BinaryImgChnlProvider getBinaryImgChnlProvider() {
		return binaryImgChnlProvider;
	}

	public void setBinaryImgChnlProvider(BinaryImgChnlProvider binaryImgChnlProvider) {
		this.binaryImgChnlProvider = binaryImgChnlProvider;
	}

	public ImageDimProvider getDimProvider() {
		return dimProvider;
	}

	public void setDimProvider(ImageDimProvider dimProvider) {
		this.dimProvider = dimProvider;
	}
}
