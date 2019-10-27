package ch.ethz.biol.cell.mpp.mark.check;

/*-
 * #%L
 * anchor-plugin-mpp
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.shared.SharedFeatureSet;

import ch.ethz.biol.cell.beaninitparams.MPPInitParams;
import ch.ethz.biol.cell.core.CheckMark;
import ch.ethz.biol.cell.mpp.mark.Mark;
import ch.ethz.biol.cell.mpp.nrg.feature.session.FeatureSessionCreateParamsMPP;

public abstract class FeatureValueCheckMark extends CheckMark {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEANS
	@BeanField
	private FeatureProvider featureProvider;
	
	@BeanField
	protected double minVal = 0;
	
	@BeanField @Optional
	private KeyValueParamsProvider keyValueParamsProvider;
	// END BEANS
	
	private SharedFeatureSet sharedFeatureSet;
	
	private Feature feature;
	
	protected FeatureSessionCreateParamsMPP session;
	
	@Override
	public void onInit(MPPInitParams soMPP) throws InitException {
		super.onInit(soMPP);
		sharedFeatureSet = soMPP.getFeature().getSharedFeatureSet();
	}
	
	
	
	@Override
	public void start(NRGStackWithParams nrgStack) throws OperationFailedException {
				
		try {
			feature = featureProvider.create();
			assert(feature!=null);
			
			KeyValueParams kpv;
			if (keyValueParamsProvider!=null) {
				kpv = keyValueParamsProvider.create();
			} else {	
				kpv = new KeyValueParams();
			}			
				
			session = new FeatureSessionCreateParamsMPP( orderedListOfFeatures(), nrgStack.getNrgStack(), nrgStack.getParams() );
			session.start( new FeatureInitParams(kpv), sharedFeatureSet, getLogger() );
		} catch (InitException | CreateException e) {
			session = null;
			throw new OperationFailedException(e);
		}
		
	}

	@Override
	public void end() {
		super.end();
	}	


	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return true;
	}

	@Override
	public FeatureList orderedListOfFeatures() throws CreateException {
		return new FeatureList(feature);
	}
	
	public double getMinVal() {
		return minVal;
	}

	public void setMinVal(double minVal) {
		this.minVal = minVal;
	}
	public FeatureProvider getFeatureProvider() {
		return featureProvider;
	}

	public void setFeatureProvider(FeatureProvider featureProvider) {
		this.featureProvider = featureProvider;
	}



	public KeyValueParamsProvider getKeyValueParamsProvider() {
		return keyValueParamsProvider;
	}



	public void setKeyValueParamsProvider(KeyValueParamsProvider keyValueParamsProvider) {
		this.keyValueParamsProvider = keyValueParamsProvider;
	}
}
