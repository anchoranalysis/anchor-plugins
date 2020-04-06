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
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.cache.FeatureSessionCacheRetriever;
import org.anchoranalysis.image.objmask.ObjMask;

import ch.ethz.biol.cell.mpp.nrg.feature.objmask.cachedcalculation.CalculateShellObjMask;

public class Shell extends DerivedObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private int iterationsDilation = 0;
	
	@BeanField
	private int iterationsErosion = 0;
	
	@BeanField
	private boolean do3D = true;
	// END BEAN PROPERTIES
	
	@Override
	protected CachedCalculation<ObjMask> createCachedCalculation(
			FeatureSessionCacheRetriever session) throws FeatureCalcException {
		return CalculateShellObjMask.createFromCache(
			session,
			iterationsDilation,
			iterationsErosion,
			0,
			do3D,
			false
		);
	}
	
	@Override
	public String cacheName() {
		return "shell" + iterationsDilation + "_" + iterationsErosion + "_" + do3D;
	}

	public int getIterationsDilation() {
		return iterationsDilation;
	}

	public void setIterationsDilation(int iterationsDilation) {
		this.iterationsDilation = iterationsDilation;
	}

	public int getIterationsErosion() {
		return iterationsErosion;
	}

	public void setIterationsErosion(int iterationsErosion) {
		this.iterationsErosion = iterationsErosion;
	}

	public boolean isDo3D() {
		return do3D;
	}

	public void setDo3D(boolean do3d) {
		do3D = do3d;
	}

}
