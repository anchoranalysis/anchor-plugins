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
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.objmask.filter.ObjMaskFilter;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.NRGStackUtilities;

// Takes features values of all objects, and discards everything outside
//   median +- (factor * stdDev)
public class ObjMaskFilterFeatureRelationDiscardFeatureRatioLessThan extends ObjMaskFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private FeatureProvider<FeatureInputSingleObj> featureProvider;
	
	@BeanField
	private double factor = 1;
	
	@BeanField
	private int minNumObjs = 1;
	
	@BeanField
	private double minStdDev = 1;
	
	@BeanField @OptionalBean
	private ChnlProvider chnlProvider;
	
	@BeanField
	private boolean includeHigherSide = true;		// Also apply the filter to the upper-side of the distribution
	
	@BeanField
	private boolean includeLowerSide = true;		// Also apply the filter to the upper-side of the distribution
	// END BEAN PROPERTIES
	
	@Override
	public void filter(ObjMaskCollection objs, ImageDim dim, ObjMaskCollection objsRejected)
			throws OperationFailedException {
		
		// Nothing to do if we don't have enough options
		if (objs.size()<minNumObjs) {
			return;
		}
		
		FeatureCalculatorSingle<FeatureInputSingleObj> session = createSession();
		
		// Now we calculate feature values for each object, and a standard deviation
		DoubleArrayList featureValsSorted = new DoubleArrayList();
		DoubleArrayList featureValsOriginalOrder = new DoubleArrayList();
		calculateVals(objs, featureValsOriginalOrder, featureValsSorted, session);

		removeOutliers(objs, objsRejected, featureValsOriginalOrder, featureValsSorted);
	}
	
	private FeatureCalculatorSingle<FeatureInputSingleObj> createSession() throws OperationFailedException {
		
		try {
			Feature<FeatureInputSingleObj> feature = featureProvider.create();
			
			FeatureCalculatorSingle<FeatureInputSingleObj> session = FeatureSession.with(
				feature,
				new FeatureInitParams(),
				getSharedObjects().getFeature().getSharedFeatureSet().downcast(),
				getLogger()
			);
						
			return NRGStackUtilities.maybeAddNrgStack(session, chnlProvider);
			
		} catch (CreateException | FeatureCalcException e) {
			throw new OperationFailedException(e);
		}
	}
	
	private void calculateVals( ObjMaskCollection objs, DoubleArrayList featureValsOriginalOrder, DoubleArrayList featureValsSorted, FeatureCalculatorSingle<FeatureInputSingleObj> session ) throws OperationFailedException {

		for( ObjMask om : objs ) {
			double featureVal;
			try {
				featureVal = session.calc(
					new FeatureInputSingleObj(om)
				);
			} catch (FeatureCalcException e) {
				throw new OperationFailedException(e);
			}
			featureValsSorted.add( featureVal );
			featureValsOriginalOrder.add( featureVal );
		}
		
		// For median
		featureValsSorted.sort();		
	}
	
	private void removeOutliers( ObjMaskCollection objs, ObjMaskCollection objsRejected, DoubleArrayList featureValsOriginalOrder, DoubleArrayList featureValsSorted ) {
				
		// Calculate standard deviation
		double sum = Descriptive.sum( featureValsSorted );
		double mean = sum/featureValsSorted.size();
		double var = Descriptive.variance( featureValsSorted.size(), sum, Descriptive.sumOfSquares( featureValsSorted ) );
		double stdDev = Descriptive.standardDeviation(var);
		
		double median = Descriptive.median(featureValsSorted);
		
		// We impose a minimum (for especially uniform objects);
		stdDev = Math.max(stdDev, minStdDev);
		
		double stdDevFactor = factor * stdDev;
		double lowerLimit = median - stdDevFactor;
		double upperLimit = median + stdDevFactor;
		
		if (getLogger()!=null) {
			getLogger().getLogReporter().log("START DiscardOutliers");
			getLogger().getLogReporter().logFormatted("median=%f   mean=%f  stdDev=%f  lowerLimit=%f  upperLimit=%f", median, mean, stdDev, lowerLimit, upperLimit);
		}
		
		List<ObjMask> listToRemove = listToRemove( objs, objsRejected, featureValsOriginalOrder, lowerLimit, upperLimit );
	
		if (getLogger()!=null) {
			getLogger().getLogReporter().log("END DiscardOutliers");
		}
		
		// NOTE This could be expensive if we have a large amount of items in the list, better to use a sorted indexed approach
		for( ObjMask om : listToRemove) {
			objs.remove(om);
		}		
	}
	
	private List<ObjMask> listToRemove( ObjMaskCollection objs, ObjMaskCollection objsRejected, DoubleArrayList featureValsOriginalOrder, double lowerLimit, double upperLimit ) {
		List<ObjMask> listToRemove = new ArrayList<>();
		for(int i=0; i<objs.size(); i++) {
			double featureVal = featureValsOriginalOrder.get(i);
			if(includeLowerSide && featureVal<lowerLimit) {
				
				ObjMask omRemove = objs.get(i);
				listToRemove.add( omRemove );
				
				if (objsRejected!=null) {
					objsRejected.add( omRemove );
				}
				
				if (getLogger()!=null) {
					getLogger().getLogReporter().logFormatted("discard with val=%f", featureVal);
				}
				
			} else if(includeHigherSide && featureVal>upperLimit) {

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
		return listToRemove;
	}
	
	public FeatureProvider<FeatureInputSingleObj> getFeatureProvider() {
		return featureProvider;
	}

	public void setFeatureProvider(FeatureProvider<FeatureInputSingleObj> featureProvider) {
		this.featureProvider = featureProvider;
	}
	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

	public int getMinNumObjs() {
		return minNumObjs;
	}

	public void setMinNumObjs(int minNumObjs) {
		this.minNumObjs = minNumObjs;
	}

	public double getMinStdDev() {
		return minStdDev;
	}

	public void setMinStdDev(double minStdDev) {
		this.minStdDev = minStdDev;
	}

	public boolean isIncludeHigherSide() {
		return includeHigherSide;
	}

	public void setIncludeHigherSide(boolean includeHigherSide) {
		this.includeHigherSide = includeHigherSide;
	}

}
