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
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public class BinaryImgChnlProviderSgmnWithMask extends BinaryImgChnlProvider {

	// START BEAN
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField @OptionalBean
	private ChnlProvider chnlProviderGradient;
	
	@BeanField
	private BinarySgmn sgmn;
	
	@BeanField
	private BinaryImgChnlProvider maskProvider;
	
	@BeanField @OptionalBean
	private HistogramProvider histogramProvider;
	// END BEAN
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	public BinaryChnl sgmn( Chnl chnl, BinarySgmnParameters params, BinaryChnl maskChnl ) throws SgmnFailedException {
		
		ObjMask omMask = new ObjMask(maskChnl.binaryVoxelBox());
	
		BinaryVoxelBox<ByteBuffer> sgmnResult = sgmn.sgmn( chnl.getVoxelBox(), params, omMask, getSharedObjects().getRandomNumberGenerator() );
		chnl.replaceVoxelBox( new VoxelBoxWrapper(sgmnResult.getVoxelBox()) );
		return new BinaryChnl( chnl, sgmnResult.getBinaryValues() );
	}
	
	
	@Override
	public BinaryChnl create() throws CreateException {
		
		Chnl chnl = chnlProvider.create();
		
		BinaryChnl maskChnl = maskProvider.create();
		
		if (!maskChnl.getDimensions().equals(chnl.getDimensions())) {
			throw new CreateException("Mask's dimensions does not match the chnlProvider's");
		}
		
		BinarySgmnParameters params = new BinarySgmnParameters();
		params.setRes(chnl.getDimensions().getRes());
		
		if (histogramProvider!=null) {
			params.setIntensityHistogram( histogramProvider.create() );
		}
		
		if (chnlProviderGradient!=null) {
			Chnl chnlGradient = chnlProviderGradient.create();
			
			if (chnlGradient!=null && !maskChnl.getDimensions().equals(chnl.getDimensions())) {
				throw new CreateException("chnlProviderGradient's dimensions does not match the chnlProvider's");
			}
			
			VoxelBox<?> vbGradient = chnlGradient.getVoxelBox().any();
			params.setVoxelBoxGradient(vbGradient);
		}
		
		try {
			return sgmn( chnl, params, maskChnl );
			
		} catch (SgmnFailedException e) {
			throw new CreateException(e);
		}
	
	}

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public BinarySgmn getSgmn() {
		return sgmn;
	}

	public void setSgmn(BinarySgmn sgmn) {
		this.sgmn = sgmn;
	}

	public ChnlProvider getChnlProviderGradient() {
		return chnlProviderGradient;
	}

	public void setChnlProviderGradient(ChnlProvider chnlProviderGradient) {
		this.chnlProviderGradient = chnlProviderGradient;
	}

	public BinaryImgChnlProvider getMaskProvider() {
		return maskProvider;
	}

	public void setMaskProvider(BinaryImgChnlProvider maskProvider) {
		this.maskProvider = maskProvider;
	}

	public HistogramProvider getHistogramProvider() {
		return histogramProvider;
	}

	public void setHistogramProvider(HistogramProvider histogramProvider) {
		this.histogramProvider = histogramProvider;
	}
}
