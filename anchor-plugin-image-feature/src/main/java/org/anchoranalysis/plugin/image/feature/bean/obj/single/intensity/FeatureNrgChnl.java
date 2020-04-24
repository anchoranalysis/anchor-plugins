package org.anchoranalysis.plugin.image.feature.bean.obj.single.intensity;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;

/**
 * A feature that uses a channel from the NRG-stack as identified by an index
 * 
 * @author Owen Feehan
 *
 */
public abstract class FeatureNrgChnl extends FeatureObjMask {

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

		if (inputSessionless.getNrgStack()==null) {
			throw new FeatureCalcException("A NRG-Stack required for this feature");
		}
		
		Chnl chnl = inputSessionless.getNrgStack().getNrgStack().getChnl(nrgIndex);
		return calcForChnl(input, chnl);
	}
	
	protected abstract double calcForChnl( SessionInput<FeatureInputSingleObj> input, Chnl chnl ) throws FeatureCalcException;
	
	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}
}
