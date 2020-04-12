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
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.ObjMask;

import ch.ethz.biol.cell.mpp.nrg.feature.objmask.cachedcalculation.CalculateShellTwoStage;

/**
 * 1. Constructs a 'shell' around an object by eroding iterationsErosion times
 * 2. Erodes the result a further iterationsErosionSecond times
 * 3. Measures the mean intensity of (Region 1. less Region 2.)
 * 
 * @author Owen Feehan
 *
 */
public class IntensityMeanShellTwoStage extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private int nrgIndex = 0;
	
	@BeanField @NonNegative
	private int iterationsErosion = 0;
	
	@BeanField @Positive
	private int iterationsFurther = 0;

	@BeanField
	private boolean do3D = true;
	// END BEAN PROPERTIES
	
	@Override
	public String getParamDscr() {
		return String.format(
			"iterationsErosion=%d,iterationsFurther=%d,do3D=%s",
			iterationsErosion,
			iterationsFurther,
			do3D ? "true" : "false"
		);
	}
		
	@Override
	public double calc(CacheableParams<FeatureObjMaskParams> params) throws FeatureCalcException {
		
		Chnl chnl = params.getParams().getNrgStack().getNrgStack().getChnl(nrgIndex);
		
		ObjMask om;
		try {
			CachedCalculation<ObjMask> ccShellTwoStage = CalculateShellTwoStage.createFromCache(
				params,
				iterationsErosion,
				iterationsFurther,
				do3D
			);
					
			om = ccShellTwoStage.getOrCalculate(params.getParams());
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}
		
		return IntensityMean.calcMeanIntensityObjMask(chnl, om );
	}
	
	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}

	public boolean isDo3D() {
		return do3D;
	}

	public void setDo3D(boolean do3d) {
		do3D = do3d;
	}

	public int getIterationsErosion() {
		return iterationsErosion;
	}

	public void setIterationsErosion(int iterationsErosion) {
		this.iterationsErosion = iterationsErosion;
	}

	
	public int getIterationsFurther() {
		return iterationsFurther;
	}


	public void setIterationsFurther(int iterationsFurther) {
		this.iterationsFurther = iterationsFurther;
	}
}
