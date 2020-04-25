package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;

public abstract class BoundingBoxAxisBase extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PARAMETERS
	@BeanField
	private String axis = "x";
	// END BEAN PARAMETERS
	
	@Override
	public double calc( SessionInput<FeatureInputSingleObj> input ) throws FeatureCalcException {
		
		FeatureInputSingleObj inputSessionless = input.get();
		
		Tuple3i pnt = extractTupleForBoundingBox(
			inputSessionless.getObjMask().getBoundingBox()
		);
		
		return calcAxisValue(
			axis.toLowerCase(),
			pnt
		);
	}
	
	protected abstract Tuple3i extractTupleForBoundingBox( BoundingBox bbox );
	
	private double calcAxisValue( String axisLowerCase, Tuple3i pnt) {
		
		if (axisLowerCase.equals("x")) {
			return pnt.getX();
		} else if (axisLowerCase.equals("y")) {
			return pnt.getY();
		} else if (axisLowerCase.equals("z")) {
			return pnt.getZ();
		} else {
			assert false;
			return -1;
		}
	}
	
	@Override
	public String getParamDscr() {
		return String.format("%s", axis);
	}
	
	public String getAxis() {
		return axis;
	}

	public void setAxis(String axis) {
		this.axis = axis;
	}
}
