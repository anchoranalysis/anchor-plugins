package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.plugin.points.calculate.ellipsoid.CalculateEllipsoidLeastSquares;

public abstract class EllipsoidBase extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	/** Iff true, Supresses covariance in the z-direction. */
	@BeanField
	private boolean suppressZCovariance = false;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(SessionInput<FeatureInputSingleObj> input) throws FeatureCalcException {
		
		ObjMask om = input.get().getObjMask();
		
		// If we have these few pixels, assume we are perfectly ellipsoid
		if (om.numPixelsLessThan(12)) {
			return 1.0;
		}
		
		MarkEllipsoid me = CalculateEllipsoidLeastSquares.createFromCache(
			input,
			suppressZCovariance
		);
		
		return calc(input.get(), me);
	}
	
	protected abstract double calc(FeatureInputSingleObj input, MarkEllipsoid me) throws FeatureCalcException;
	
	public boolean isSuppressZCovariance() {
		return suppressZCovariance;
	}

	public void setSuppressZCovariance(boolean suppressZCovariance) {
		this.suppressZCovariance = suppressZCovariance;
	}
}
