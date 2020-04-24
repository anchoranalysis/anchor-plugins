package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;

public abstract class IntensityMeanBase extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private int nrgIndex = 0;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(SessionInput<FeatureInputSingleObj> input) throws FeatureCalcException {
		
		FeatureInputSingleObj inputSessionless = input.get();

		return calcForChnl(
			input,
			inputSessionless.getNrgStack().getNrgStack().getChnl(nrgIndex)
		);
	}
	
	protected abstract double calcForChnl( SessionInput<FeatureInputSingleObj> input, Chnl chnl ) throws FeatureCalcException;
	
	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}
}
