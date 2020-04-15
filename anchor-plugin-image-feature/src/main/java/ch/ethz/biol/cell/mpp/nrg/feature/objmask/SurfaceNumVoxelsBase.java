package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;

public abstract class SurfaceNumVoxelsBase extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private boolean mip=false;
	
	@BeanField
	private boolean suppress3D=false;
	/// END BEAN PROPERTIES
	
	@Override
	public double calc(CacheableParams<FeatureObjMaskParams> params) throws FeatureCalcException {
		try {
			return params.calc(
				createParams(mip, suppress3D)		
			);
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e.getCause());
		}
	}
	
	protected abstract CachedCalculation<Integer,FeatureObjMaskParams> createParams(boolean mip, boolean suppress3d);

	public boolean isMip() {
		return mip;
	}

	public void setMip(boolean mip) {
		this.mip = mip;
	}

	public boolean isSuppress3D() {
		return suppress3D;
	}

	public void setSuppress3D(boolean suppress3d) {
		suppress3D = suppress3d;
	}
}