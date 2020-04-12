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


// From Page 727 from Lin et al (A Multi-Model Approach to Simultaneous Segmentation and Classification of Heterogeneous Populations of Cell Nuclei
//   in 3D Confocal Microscope Images, 2004)
public class TextureScore extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private int nrgIndex = 0;
	
	@BeanField
	private int nrgIndexGradient = 1;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(CacheableParams<FeatureObjMaskParams> paramsCacheable) throws FeatureCalcException {
		
		FeatureObjMaskParams params = paramsCacheable.getParams();
		
		Chnl chnl = params.getNrgStack().getNrgStack().getChnl(nrgIndex);
		
		double meanIntensity = IntensityMean.calcMeanIntensityObjMask(chnl, params.getObjMask() );
		
		Chnl chnlGradient = params.getNrgStack().getNrgStack().getChnl(nrgIndexGradient);
		
		
		double meanGradientIntensity = IntensityMean.calcMeanIntensityObjMask(chnlGradient, params.getObjMask() );
		
		double scaleFactor = 128 / meanIntensity;
		//double scaleFactor = 1;
		
		
		
		return (scaleFactor*meanGradientIntensity)/meanIntensity;
	}

	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}

	public int getNrgIndexGradient() {
		return nrgIndexGradient;
	}

	public void setNrgIndexGradient(int nrgIndexGradient) {
		this.nrgIndexGradient = nrgIndexGradient;
	}

}
