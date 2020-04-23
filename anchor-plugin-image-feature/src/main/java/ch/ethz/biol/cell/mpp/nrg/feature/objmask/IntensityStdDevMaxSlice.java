package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;

import ch.ethz.biol.cell.mpp.nrg.feature.objmask.IntensityMeanMaxSlice.ValueAndIndex;

public class IntensityStdDevMaxSlice extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private int nrgIndex = 0;
	
	@BeanField
	private boolean excludeZero = false;
	
	@BeanField
	private double emptyValue = 0;
	// END BEAN PROPERTIES
	
	private static double calcStdDev( Chnl chnl, ObjMask om, boolean excludeZero, int z, double emptyValue ) throws FeatureCalcException {
			
		ObjMask omSlice;
		try {
			omSlice = om.extractSlice(z, true);
		} catch (OperationFailedException e) {
			throw new FeatureCalcException(e);
		}
		
		// We adjust the z coordiante to point to the channel
		int oldZ = omSlice.getBoundingBox().getCrnrMin().getZ();
		omSlice.getBoundingBox().getCrnrMin().setZ( oldZ + om.getBoundingBox().getCrnrMin().getZ() );
		
		return IntensityStdDev.calcStdDev(omSlice, chnl, excludeZero, emptyValue);
	}
	

	@Override
	public double calc(SessionInput<FeatureInputSingleObj> paramsCacheable) throws FeatureCalcException {
		
		FeatureInputSingleObj params = paramsCacheable.getParams();
		
		Chnl chnl = params.getNrgStack().getNrgStack().getChnl(nrgIndex);
		
		ValueAndIndex vai = IntensityMeanMaxSlice.calcMeanIntensityObjMask(chnl, params.getObjMask(), excludeZero );
		
		if (vai.getIndex()==-1) {
			return emptyValue;
		}
		
		double sd = calcStdDev( chnl, params.getObjMask(), excludeZero, vai.getIndex(), emptyValue );
		
		return sd;
	}

	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}


	public boolean isExcludeZero() {
		return excludeZero;
	}


	public void setExcludeZero(boolean excludeZero) {
		this.excludeZero = excludeZero;
	}



	public double getEmptyValue() {
		return emptyValue;
	}



	public void setEmptyValue(double emptyValue) {
		this.emptyValue = emptyValue;
	}

}
