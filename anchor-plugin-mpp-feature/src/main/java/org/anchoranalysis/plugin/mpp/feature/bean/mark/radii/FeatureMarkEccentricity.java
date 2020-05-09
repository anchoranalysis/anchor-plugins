package org.anchoranalysis.plugin.mpp.feature.bean.mark.radii;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMark;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractRadii;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.orientation.Orientation;

public abstract class FeatureMarkEccentricity extends FeatureMark {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double calc(SessionInput<FeatureInputMark> input) throws FeatureCalcException {

		Mark mark = input.get().getMark();
		
		if (!(mark instanceof MarkAbstractRadii)) {
			throw new FeatureCalcException("mark must be of type MarkAbstractRadii");
		}
		
		double[] radii = ((MarkAbstractRadii) mark).radiiOrdered();
		
		if (radii.length==2) {		
			return calcEccentricityForEllipse(radii);
		} else {
			
			if (!(mark instanceof MarkEllipsoid)) {
				throw new FeatureCalcException("mark must be of type MarkEllipsoid");
			}	
			
			return calcEccentricityForEllipsoid(
				radii,
				((MarkEllipsoid) mark).getOrientation()
			);
		}
	}
	
	/**
	 * Calculates eccentricity in 3D-case
	 * 
	 * @param radii an array of length 3, ordered from largest radius to smallest
	 * @return the eccentricity
	 */
	protected abstract double calcEccentricityForEllipsoid( double[] radii, Orientation orientation );
	
	/**
	 * Calculates eccentricity in 2D-case
	 * 
	 * @param radii an array of length 2, ordered from largest radius to smallest
	 * @return the eccentricity
	 */
	private double calcEccentricityForEllipse( double[] radii ) {
		return calcEccentricity(radii[1], radii[0]);
	}
	
	/**
	 * Calculates eccentricity given two axes
	 * 
	 * @param semiMajorAxis length of semi-major axis
	 * @param semiMinorAxis length of semi-minor axis
	 * @return the eccentricity
	 */
	protected static double calcEccentricity( double semiMajorAxis, double semiMinorAxis ) {
		double ratio = semiMinorAxis/semiMajorAxis;
		return Math.sqrt( 1.0 - Math.pow(ratio, 2.0) );
	}
}