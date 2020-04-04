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

public class IntensityQuantile extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private int nrgIndex = 0;
	
	@BeanField
	private double quantile = 0;
	// END BEAN PROPERTIES
	
	public static double calcQuantileIntensityObjMask( Chnl chnl, ObjMask om, double quantile ) {
		
		Histogram h = HistogramFactoryUtilities.create(chnl, om);
		return h.quantile(quantile);
	}
	

	@Override
	public double calcCast(CacheableParams<FeatureObjMaskParams> paramsCacheable) throws FeatureCalcException {
		
		FeatureObjMaskParams params = paramsCacheable.getParams();
		
		Chnl chnl = params.getNrgStack().getNrgStack().getChnl(nrgIndex);
		return calcQuantileIntensityObjMask(chnl, params.getObjMask(), quantile );
	}

	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}

	public double getQuantile() {
		return quantile;
	}

	public void setQuantile(double quantile) {
		this.quantile = quantile;
	}

}
