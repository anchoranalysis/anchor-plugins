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


import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectMaskCollection;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class ChnlProviderObjMaskFeature extends ChnlProviderOneObjsSource {

	// START BEAN PROPERTIES
	@BeanField
	private int valueNoObject = 0;
	
	@BeanField
	private FeatureProvider<FeatureInputSingleObj> featureProvider;
	
	@BeanField
	private List<ChnlProvider> listAdditionalChnlProviders = new ArrayList<>();
	
	@BeanField
	private double factor = 1.0;
	// END BEAN PROPERTIES
	
	@Override
	protected Channel createFromChnl(Channel chnl, ObjectMaskCollection objsSource) throws CreateException {

		Feature<FeatureInputSingleObj> feature = featureProvider.create();
		
		try {
			NRGStack nrgStack = createNrgStack(chnl);
						
			return createOutputChnl(
				chnl.getDimensions(),
				objsSource,
				createSession(feature),
				new NRGStackWithParams(nrgStack)
			);
						
		} catch (FeatureCalcException e) {
			throw new CreateException(e);
		}
	}
	
	private NRGStack createNrgStack(Channel chnl) throws CreateException {
		NRGStack nrgStack = new NRGStack(chnl);
		
		// add other channels
		for( ChnlProvider cp : listAdditionalChnlProviders ) {
			Channel chnlAdditional = cp.create();
			
			if (!chnlAdditional.getDimensions().equals(chnl.getDimensions())) {
				throw new CreateException("Dimensions of additional channel are not equal to main channel");
			}

			try {
				nrgStack.asStack().addChnl( chnlAdditional );
			} catch (IncorrectImageSizeException e) {
				throw new CreateException(e);
			}
		}
		
		return nrgStack;
	}
	
	private FeatureCalculatorSingle<FeatureInputSingleObj> createSession(Feature<FeatureInputSingleObj> feature) throws FeatureCalcException {
		return FeatureSession.with(
			feature,
			new FeatureInitParams(),
			getSharedObjects().getFeature().getSharedFeatureSet(),
			getLogger()
		);
	}
	
	private Channel createOutputChnl(
		ImageDim dim,
		ObjectMaskCollection objsSource,
		FeatureCalculatorSingle<FeatureInputSingleObj> session,
		NRGStackWithParams nrgStackParams
	) throws FeatureCalcException {
		Channel chnlOut = ChannelFactory.instance().createEmptyInitialised( dim, VoxelDataTypeUnsignedByte.instance );
		chnlOut.getVoxelBox().any().setAllPixelsTo( valueNoObject );
		for( ObjectMask om : objsSource ) {

			double featVal = session.calc(
				new FeatureInputSingleObj(om, nrgStackParams)
			);
			chnlOut.getVoxelBox().any().setPixelsCheckMask(om, (int) (factor*featVal) );
		}
		
		return chnlOut;		
	}

	public FeatureProvider<FeatureInputSingleObj> getFeatureProvider() {
		return featureProvider;
	}

	public void setFeatureProvider(FeatureProvider<FeatureInputSingleObj> featureProvider) {
		this.featureProvider = featureProvider;
	}

	public List<ChnlProvider> getListAdditionalChnlProviders() {
		return listAdditionalChnlProviders;
	}

	public void setListAdditionalChnlProviders(
			List<ChnlProvider> listAdditionalChnlProviders) {
		this.listAdditionalChnlProviders = listAdditionalChnlProviders;
	}

	public int getValueNoObject() {
		return valueNoObject;
	}

	public void setValueNoObject(int valueNoObject) {
		this.valueNoObject = valueNoObject;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}
}
