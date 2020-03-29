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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

import ch.ethz.biol.cell.imageprocessing.dim.provider.GuessDimFromInputImage;

public class BinaryImgChnlProviderEmpty extends BinaryImgChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private ImageDimProvider dimProvider = new GuessDimFromInputImage();
	
	/**
	 * If true binary values are set high when created
	 */
	@BeanField
	private boolean createOn = false;
	// END BEAN PROPERTIES
	
	@Override
	public BinaryChnl create() throws CreateException {
		
		ImageDim sd = dimProvider.create();
		
		Chnl chnl = ChnlFactory.instance().createEmptyInitialised(sd, VoxelDataTypeUnsignedByte.instance);

		BinaryValues bvOut = BinaryValues.getDefault();
		 
		if (createOn) {
			chnl.getVoxelBox().any().setAllPixelsTo( bvOut.getOnInt() );	
		}
		return new BinaryChnl(chnl, bvOut );
	}

	public boolean isCreateOn() {
		return createOn;
	}

	public void setCreateOn(boolean createOn) {
		this.createOn = createOn;
	}

	public ImageDimProvider getDimProvider() {
		return dimProvider;
	}

	public void setDimProvider(ImageDimProvider dimProvider) {
		this.dimProvider = dimProvider;
	}
}
