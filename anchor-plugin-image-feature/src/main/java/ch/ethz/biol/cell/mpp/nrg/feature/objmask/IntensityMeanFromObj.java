package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;

public abstract class IntensityMeanFromObj extends FeatureNrgChnl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected double calcForChnl(SessionInput<FeatureInputSingleObj> input, Chnl chnl)
			throws FeatureCalcException {
		return calcForMaskedChnl(chnl, input.get().getObjMask());
	}

	protected abstract double calcForMaskedChnl( Chnl chnl, ObjMask mask ) throws FeatureCalcException;
}
