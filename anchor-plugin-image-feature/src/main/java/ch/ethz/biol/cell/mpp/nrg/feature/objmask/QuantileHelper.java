package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactoryUtilities;
import org.anchoranalysis.image.objmask.ObjMask;

class QuantileHelper {
	
	public static double calcQuantileIntensityObjMask( Chnl chnl, ObjMask om, double quantile ) {
		
		Histogram h = HistogramFactoryUtilities.create(chnl, om);
		return h.quantile(quantile);
	}
}
