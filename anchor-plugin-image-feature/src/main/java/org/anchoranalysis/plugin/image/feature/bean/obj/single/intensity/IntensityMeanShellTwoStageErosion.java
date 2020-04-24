package org.anchoranalysis.plugin.image.feature.bean.obj.single.intensity;

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
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
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
public class IntensityMeanShellTwoStageErosion extends IntensityMeanShellBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField @Positive
	private int iterationsFurther = 0;
	// END BEAN PROPERTIES
	
	@Override
	public String getParamDscr() {
		return String.format(
			"%s,iterationsFurther=%d",
			iterationsFurther
		);
	}

	@Override
	protected double calcForChnl(SessionInput<FeatureInputSingleObj> input, Chnl chnl) throws FeatureCalcException {
		
		ObjMask om;
		try {
			ResolvedCalculation<ObjMask,FeatureInputSingleObj> ccShellTwoStage = CalculateShellTwoStage.createFromCache(
				input.resolver(),
				getIterationsErosion(),
				iterationsFurther,
				isDo3D()
			);
					
			om = ccShellTwoStage.getOrCalculate(input.get());
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}
		
		return IntensityMeanCalculator.calcMeanIntensityObjMask(chnl, om );
	}
	
	public int getIterationsFurther() {
		return iterationsFurther;
	}

	public void setIterationsFurther(int iterationsFurther) {
		this.iterationsFurther = iterationsFurther;
	}
}