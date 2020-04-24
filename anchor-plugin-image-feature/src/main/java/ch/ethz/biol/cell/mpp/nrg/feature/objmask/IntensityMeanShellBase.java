package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonNegative;

public abstract class IntensityMeanShellBase extends IntensityMeanBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField @NonNegative
	private int iterationsErosion = 0;
		
	@BeanField
	private boolean do3D = true;
	// END BEAN PROPERTIES
	
	@Override
	public String getParamDscr() {
		return String.format(
			"iterationsErosion=%d,do3D=%s",
			iterationsErosion,
			do3D ? "true" : "false"
		);
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
