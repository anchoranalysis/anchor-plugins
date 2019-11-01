package ch.ethz.biol.cell.mpp.nrg.feature.ind;

import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;

import ch.ethz.biol.cell.mpp.nrg.NRGElemInd;
import ch.ethz.biol.cell.mpp.nrg.NRGElemIndCalcParams;

public class MaxRatioRadii extends NRGElemInd {

	// START BEAN PROPERTIES
	// END BEAN PROPERTIES
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double calcCast( NRGElemIndCalcParams params ) {
		
		MarkEllipse mark = (MarkEllipse) params.getPxlPartMemo().getMark();
		
		double rad1 = mark.getRadii().getX();
		double rad2 = mark.getRadii().getY();
		
		assert( !Double.isNaN( mark.getRadii().getX()) );
		assert( !Double.isNaN( mark.getRadii().getY()) );
		
		if (rad1==0 || rad2==0) {
			return 0.0;
		}
		
		return Math.max( rad1/rad2, rad2/rad1 );
	}
}
