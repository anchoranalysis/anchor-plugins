package ch.ethz.biol.cell.mpp.nrg.feature.stack;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.axis.AxisType;
import org.anchoranalysis.core.axis.AxisTypeConverter;
import org.anchoranalysis.image.extent.ImageDim;

public abstract class FeatureStackForAxis extends FeatureStackFromDimensions {

	// START BEAN PARAMETERS
	@BeanField
	private String axis = "x";
	// END BEAN PARAMETERS

	@Override
	protected double calcFromDims(ImageDim dim) {
		return calcForAxis(
			dim,
			AxisTypeConverter.createFromString(axis)
		);
	}
	
	protected abstract double calcForAxis( ImageDim dim, AxisType axis );
	
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
