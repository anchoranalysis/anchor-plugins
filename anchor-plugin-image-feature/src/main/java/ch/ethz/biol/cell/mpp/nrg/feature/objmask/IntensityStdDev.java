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
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactoryUtilities;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.statistics.VoxelStatisticsFromHistogram;

public class IntensityStdDev extends FeatureObjMask {

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
	private double emptyValue = Double.NaN;
	// END BEAN PROPERTIES
	
	public static double calcStdDev( ObjMask objMask, Chnl chnl, boolean ignoreZero, double emptyValue ) {
		Histogram hist = HistogramFactoryUtilities.createHistogramIgnoreZero(chnl,objMask,ignoreZero);
		
		if (hist.getTotalCount()==0) {
			return emptyValue;
		}
		
		return new VoxelStatisticsFromHistogram(hist).stdDev();
	}
	
	@Override
	public double calcCast(CacheableParams<FeatureObjMaskParams> paramsCacheable) throws FeatureCalcException {
		
		FeatureObjMaskParams params = paramsCacheable.getParams();
		
		if (params.getNrgStack()==null) {
			throw new FeatureCalcException("NrgStack required");
		}
		
		Chnl chnl = params.getNrgStack().getNrgStack().getChnl(nrgIndex);
		
		return calcStdDev( params.getObjMask(), chnl, excludeZero, emptyValue );
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
