package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

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
