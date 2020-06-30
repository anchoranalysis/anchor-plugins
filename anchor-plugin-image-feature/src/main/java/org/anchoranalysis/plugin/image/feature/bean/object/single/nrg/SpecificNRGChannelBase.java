package org.anchoranalysis.plugin.image.feature.bean.object.single.nrg;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;

public abstract class SpecificNRGChannelBase extends FeatureSingleObject {

	// START BEAN PROPERTIES
	/** Index of which channel in the nrg-stack to select */ 
	@BeanField
	private int nrgIndex = 0;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {
		return calcWithChannel(
			input.get().getObjectMask(),
			input.get().getNrgStackRequired().getChnl(nrgIndex)
		);
	}
	
	protected abstract double calcWithChannel(ObjectMask obj, Channel chnl) throws FeatureCalcException;
	
	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}
}
