package ch.ethz.biol.cell.imageprocessing.objmask.filter;

/*
 * #%L
 * anchor-plugin-image-feature
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
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.objmask.filter.ObjMaskFilter;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.session.FeatureSessionCreateParamsSingle;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

// Takes features values of all objects, and discards everything outside
//   median +- (factor * stdDev)
public class ObjMaskFilterFeatureRelationDiscardOutliers extends ObjMaskFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private FeatureProvider featureProvider;
	
	@BeanField @Optional
	private ChnlProvider chnlProvider;
	
	@BeanField
	private double quantile;
	
	@BeanField
	private double minRatio;
	
	@BeanField
	private int minNumObjs = 1;
	// END BEAN PROPERTIES
	
	@Override
	public void filter(ObjMaskCollection objs, ImageDim dim, ObjMaskCollection objsRejected)
			throws OperationFailedException {

		// Nothing to do if we don't have enough options
		if (objs.size()<minNumObjs) {
			return;
		}
		
		// Initialization
		FeatureSessionCreateParamsSingle session;
		Feature feature;
		{
			try {
				feature = featureProvider.create();
			} catch (CreateException e) {
				throw new OperationFailedException(e);
			}
			
			session = new FeatureSessionCreateParamsSingle( feature,getSharedObjects().getFeature().getSharedFeatureSet() );
			
			try {
				session.start( getLogger() );
			} catch (InitException e) {
				throw new OperationFailedException(e);
			}
			
			if (chnlProvider!=null) {
				try {
					NRGStackWithParams nrgStack = new NRGStackWithParams(chnlProvider.create());
					session.setNrgStack(nrgStack);
				} catch (CreateException e) {
					throw new OperationFailedException(e);
				}
			}
		}
		
		// Now we calculate feature values for each object, and a standard deviation
		DoubleArrayList featureValsSorted = new DoubleArrayList();
		DoubleArrayList featureValsOriginalOrder = new DoubleArrayList();
		for( ObjMask om : objs ) {
			double featureVal;
			try {
				featureVal = session.calc(om);
			} catch (FeatureCalcException e) {
				throw new OperationFailedException(e);
			}
			featureValsSorted.add( featureVal );
			featureValsOriginalOrder.add( featureVal );
		}
		
		// For median
		featureValsSorted.sort();
		
		double quantileVal = Descriptive.quantile(featureValsSorted,quantile);
		
		double minVal = quantileVal * minRatio;
		
		
		if (getLogger()!=null) {
			getLogger().getLogReporter().log("START DiscardOutliers");
			getLogger().getLogReporter().logFormatted("quantileVal(%f)=%f   minVal=%f", quantile,quantileVal, minVal);
		}
			
		List<ObjMask> listToRemove = new ArrayList<>();
		for(int i=0; i<objs.size(); i++) {
			double featureVal = featureValsOriginalOrder.get(i);
			
			if(featureVal<minVal) {
				
				ObjMask omRemove = objs.get(i);
				listToRemove.add( omRemove );
				
				if (objsRejected!=null) {
					objsRejected.add( omRemove );
				}
				
				if (getLogger()!=null) {
					getLogger().getLogReporter().logFormatted("discard with val=%f", featureVal);
				}
				
			}
		}
		
		if (getLogger()!=null) {
			getLogger().getLogReporter().log("END DiscardOutliers");
		}
		
		// NOTE This could be expensive if we have a large amount of items in the list, better to use a sorted indexed approach
		for( ObjMask om : listToRemove) {
			objs.remove(om);
		}
	}
	
	public FeatureProvider getFeatureProvider() {
		return featureProvider;
	}

	public void setFeatureProvider(FeatureProvider featureProvider) {
		this.featureProvider = featureProvider;
	}
	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public int getMinNumObjs() {
		return minNumObjs;
	}

	public void setMinNumObjs(int minNumObjs) {
		this.minNumObjs = minNumObjs;
	}

	public double getQuantile() {
		return quantile;
	}

	public void setQuantile(double quantile) {
		this.quantile = quantile;
	}

	public double getMinRatio() {
		return minRatio;
	}

	public void setMinRatio(double minRatio) {
		this.minRatio = minRatio;
	}

	

}
