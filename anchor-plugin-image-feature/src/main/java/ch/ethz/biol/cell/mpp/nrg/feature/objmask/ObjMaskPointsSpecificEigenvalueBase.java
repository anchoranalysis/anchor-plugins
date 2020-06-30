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
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.math.moment.EigenvalueAndVector;
import org.anchoranalysis.math.moment.MomentsFromPointsCalculator;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.moment.CalculateObjMaskPointsSecondMomentMatrix;

public abstract class ObjMaskPointsSpecificEigenvalueBase extends FeatureSingleObject {

	// START BEAN PROPERTIES
	@BeanField
	private int eigenvalueIndex = 0;
	// END BEAN PROPERTIES
		
	protected EigenvalueAndVector calcSpecificEigenvector( SessionInput<FeatureInputSingleObject> params ) throws FeatureCalcException {
		
		MomentsFromPointsCalculator mm = params.calc(
			new CalculateObjMaskPointsSecondMomentMatrix(false)			
		);
		return mm.get(eigenvalueIndex );
	}
	
	public int getEigenvalueIndex() {
		return eigenvalueIndex;
	}

	public void setEigenvalueIndex(int eigenvalueIndex) {
		this.eigenvalueIndex = eigenvalueIndex;
	}
}
