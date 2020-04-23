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
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;

public class IntensityMeanMaxSlice extends FeatureObjMask {

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
	private int emptyValue = 0;
	// END BEAN PROPERTIES
	
	public static class ValueAndIndex {
		private double value;
		private int index;
		
		public ValueAndIndex(double value, int index) {
			super();
			this.value = value;
			this.index = index;
		}

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}
		
		
	}
	
	public static ValueAndIndex calcMeanIntensityObjMask( Chnl chnl, ObjMask om, boolean excludeZero ) throws FeatureCalcException {
		
		double max = Double.NEGATIVE_INFINITY;
		int index = -1;
		
		for( int z=0; z<om.getBoundingBox().extnt().getZ(); z++ ) {
			
			ObjMask omSlice;
			try {
				omSlice = om.extractSlice(z, true);
			} catch (OperationFailedException e) {
				throw new FeatureCalcException(e);
			}
			
			// We adjust the z coordiante to point to the channel
			int oldZ = omSlice.getBoundingBox().getCrnrMin().getZ();
			omSlice.getBoundingBox().getCrnrMin().setZ( oldZ + om.getBoundingBox().getCrnrMin().getZ() );
			
			if (omSlice.hasPixelsGreaterThan(0)) {
				double mean = IntensityMean.calcMeanIntensityObjMask(chnl, omSlice, excludeZero);
				
				if (mean>max) {
					index = z;
					max = mean;
				}
			}
		}
		
		return new ValueAndIndex(max,index);
	}
	

	@Override
	public double calc(CacheableParams<FeatureInputSingleObj> paramsCacheable) throws FeatureCalcException {
		
		FeatureInputSingleObj params = paramsCacheable.getParams();
		
		Chnl chnl = params.getNrgStack().getNrgStack().getChnl(nrgIndex);
		
		ValueAndIndex vai = calcMeanIntensityObjMask(chnl, params.getObjMask(), excludeZero );
		
		if (vai.getIndex()==-1) {
			return emptyValue;
		}
		
		return vai.getValue();
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


	public int getEmptyValue() {
		return emptyValue;
	}


	public void setEmptyValue(int emptyValue) {
		this.emptyValue = emptyValue;
	}

}
