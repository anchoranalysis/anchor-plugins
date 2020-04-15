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


import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;

import ch.ethz.biol.cell.mpp.nrg.feature.objmask.cachedcalculation.CalculateGradientFromMultipleChnls;

public abstract class IntensityGradientBase extends FeatureObjMask {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField @NonNegative
	private int nrgIndexX = -1;
	
	@BeanField @NonNegative
	private int nrgIndexY = -1;
	
	@BeanField @OptionalBean
	private int nrgIndexZ = -1;
	
	@BeanField
	private int subtractConstant = 0;
	// END BEAN PROPERTIES
	
	protected CachedCalculation<List<Point3d>,FeatureObjMaskParams> gradientCalculation() {
		return new CalculateGradientFromMultipleChnls(nrgIndexX,nrgIndexY,nrgIndexZ,subtractConstant);
	}
	
	public int getNrgIndexX() {
		return nrgIndexX;
	}

	public void setNrgIndexX(int nrgIndexX) {
		this.nrgIndexX = nrgIndexX;
	}

	public int getNrgIndexY() {
		return nrgIndexY;
	}

	public void setNrgIndexY(int nrgIndexY) {
		this.nrgIndexY = nrgIndexY;
	}

	public int getNrgIndexZ() {
		return nrgIndexZ;
	}

	public void setNrgIndexZ(int nrgIndexZ) {
		this.nrgIndexZ = nrgIndexZ;
	}

	public int getSubtractConstant() {
		return subtractConstant;
	}

	public void setSubtractConstant(int subtractConstant) {
		this.subtractConstant = subtractConstant;
	}
}
