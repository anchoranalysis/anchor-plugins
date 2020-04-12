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
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.SessionFactory;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.pixelwise.PixelwiseFeatureInitParams;
import org.anchoranalysis.image.feature.pixelwise.score.PixelScoreFeatureCalcParams;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public class ChnlProviderPixelScoreFeature extends ChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private FeatureProvider<PixelScoreFeatureCalcParams> featureProvider;
	
	@BeanField
	private List<ChnlProvider> listAdditionalChnlProviders = new ArrayList<>();
	
	@BeanField @Optional
	private HistogramProvider histogramProvider;
	// END BEAN PROPERTIES
	
	@Override
	public Chnl create() throws CreateException {

		Feature<PixelScoreFeatureCalcParams> feature = featureProvider.create();
		
		PixelwiseFeatureInitParams paramsInit = new PixelwiseFeatureInitParams(getSharedObjects().getRandomNumberGenerator() );
		
		if (histogramProvider!=null) {
			paramsInit.addListHist( histogramProvider.create() );
		}
	
		Chnl chnl = chnlProvider.create();
		
		List<Chnl> listAdditional = additionalChnls( chnl.getDimensions() );
		
		try {
			FeatureCalculatorSingle<PixelScoreFeatureCalcParams> calculator = SessionFactory.createAndStart(
				feature,
				paramsInit,
				getSharedObjects().getFeature().getSharedFeatureSet().downcast(),
				getLogger()
			);
			
			calcScoresIntoVoxelBox( chnl.getVoxelBox(), listAdditional, calculator );
			
		} catch (FeatureCalcException e2) {
			throw new CreateException(e2);
		}
				 
		return chnl;
	}
	
	private static void calcScoresIntoVoxelBox(
		VoxelBoxWrapper vb,
		List<Chnl> listAdditional,
		FeatureCalculatorSingle<PixelScoreFeatureCalcParams> featureCalculator
	) throws FeatureCalcException {
		
		ByteBuffer[] arrByteBuffer = new ByteBuffer[ listAdditional.size() ];
		
		Extent e = vb.any().extnt();
		for( int z=0; z<e.getZ(); z++) {
			
			VoxelBuffer<?> bb = vb.any().getPixelsForPlane(z);
			
			for( int i=0; i<listAdditional.size(); i++) {
				Chnl additional = listAdditional.get(i);
				arrByteBuffer[i] = additional.getVoxelBox().asByte().getPixelsForPlane(z).buffer();
			}
			
			int offset = 0;
			for( int y=0; y<e.getY(); y++) {
				for( int x=0; x<e.getX(); x++) {
					
					double result = featureCalculator.calcOne(
						createParams(bb, arrByteBuffer, offset)
					);
					
					int valOut = (int) Math.round(result);
					
					if (valOut<0) valOut=0;
					if (valOut>255) valOut=255;
					
					bb.putInt(offset, valOut );
					
					offset++;
				}
			}
		}
	}
	
	private static PixelScoreFeatureCalcParams createParams( VoxelBuffer<?> bufferPrimary, ByteBuffer[] buffersAdd, int offset ) {
		PixelScoreFeatureCalcParams params = new PixelScoreFeatureCalcParams( 1 + buffersAdd.length );
		
		int val = bufferPrimary.getInt(offset);
		params.setPxl(0, val);
		
		int i = 1;
		for( ByteBuffer bbAdd : buffersAdd) {
			params.setPxl(i++, bbAdd.getInt(offset) );
		}
		return params;
	}

	private List<Chnl> additionalChnls( ImageDim dim ) throws CreateException {
		List<Chnl> listAdditional = new ArrayList<>();
		for( ChnlProvider cp : listAdditionalChnlProviders ) {
			Chnl chnlAdditional = cp.create();
			
			if (!chnlAdditional.getDimensions().equals(dim)) {
				throw new CreateException("Dimensions of additional channel are not equal to main channel");
			}
			
			listAdditional.add( chnlAdditional);
		}
		return listAdditional;
	}
	
	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public FeatureProvider<PixelScoreFeatureCalcParams> getFeatureProvider() {
		return featureProvider;
	}

	public void setFeatureProvider(FeatureProvider<PixelScoreFeatureCalcParams> featureProvider) {
		this.featureProvider = featureProvider;
	}

	public List<ChnlProvider> getListAdditionalChnlProviders() {
		return listAdditionalChnlProviders;
	}

	public void setListAdditionalChnlProviders(
			List<ChnlProvider> listAdditionalChnlProviders) {
		this.listAdditionalChnlProviders = listAdditionalChnlProviders;
	}

	public HistogramProvider getHistogramProvider() {
		return histogramProvider;
	}

	public void setHistogramProvider(HistogramProvider histogramProvider) {
		this.histogramProvider = histogramProvider;
	}



}
