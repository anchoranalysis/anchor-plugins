package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.math.moment.MomentsFromPointsCalculator;
import org.anchoranalysis.points.moment.CalculateObjMaskPointsSecondMomentMatrix;

public abstract class AxisMomentsBase extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private boolean suppressZCovariance = false;		// Supresses covariance in the z-direction.
	// END BEAN PROPERTIES
	
	@Override
	public double calc(SessionInput<FeatureInputSingleObj> input) throws FeatureCalcException {
		
		FeatureInputSingleObj params = input.get();
		
		// Max intensity projection of the input mask
		ObjMask om = params.getObjMask();

		// If we have these few pixels, assume we are perfectly ellipsoid
		if (om.numPixelsLessThan(12)) {
			return 1.0;
		}
		
		MomentsFromPointsCalculator moments = calcMoments(input);
		return calcFromMoments(moments);
	}
	
	protected abstract double calcFromMoments( MomentsFromPointsCalculator moments ) throws FeatureCalcException;
	
	private MomentsFromPointsCalculator calcMoments(SessionInput<FeatureInputSingleObj> input) throws FeatureCalcException {
		MomentsFromPointsCalculator moments = input.calc(
			new CalculateObjMaskPointsSecondMomentMatrix(suppressZCovariance)	
		);
		
		moments = moments.duplicate();
		
		// Disconsider the z moment
		moments.removeClosestToUnitZ();
		return moments;
	}
		
	public boolean isSuppressZCovariance() {
		return suppressZCovariance;
	}

	public void setSuppressZCovariance(boolean suppressZCovariance) {
		this.suppressZCovariance = suppressZCovariance;
	}
}
