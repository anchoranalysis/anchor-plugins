package ch.ethz.biol.cell.mpp.nrg.feature.mark;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMark;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMarkParams;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Vector3d;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.image.bean.orientation.DirectionVectorBean;
import org.anchoranalysis.image.orientation.DirectionVector;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.math.rotation.RotationMatrix;

public abstract class DirectionVectorBase extends FeatureMark {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private DirectionVectorBean directionVector;
	// END BEAN PROPERTIES
	
	private DirectionVector dv;
	
	@Override
	public void beforeCalc(FeatureInitParams params) throws InitException {
		super.beforeCalc(params);
		dv = directionVector.createVector();
	}
	
	@Override
	public double calc(FeatureMarkParams params) throws FeatureCalcException {

		if (!(params.getMark() instanceof MarkEllipsoid)) {
			throw new FeatureCalcException("Only supports MarkEllipsoids");
		}
		
		MarkEllipsoid mark = (MarkEllipsoid) params.getMark();
		
		Orientation orientation = mark.getOrientation();
		RotationMatrix rotMatrix = orientation.createRotationMatrix();
		return calcForEllipsoid(
			mark,
			orientation,
			rotMatrix, dv.createVector3d()
		);
		
	}
	
	protected abstract double calcForEllipsoid(MarkEllipsoid mark, Orientation orientation, RotationMatrix rotMatrix, Vector3d directionVector) throws FeatureCalcException;
	

	public DirectionVectorBean getDirectionVector() {
		return directionVector;
	}


	public void setDirectionVector(DirectionVectorBean directionVector) {
		this.directionVector = directionVector;
	}
}
