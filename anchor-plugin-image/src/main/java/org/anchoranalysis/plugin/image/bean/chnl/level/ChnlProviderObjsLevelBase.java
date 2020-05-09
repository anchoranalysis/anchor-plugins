package org.anchoranalysis.plugin.image.bean.chnl.level;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;

public abstract class ChnlProviderObjsLevelBase extends ChnlProviderLevel {

	// START BEAN
	@BeanField
	private CalculateLevel calculateLevel;
	// END BEAN
		
	public CalculateLevel getCalculateLevel() {
		return calculateLevel;
	}

	public void setCalculateLevel(CalculateLevel calculateLevel) {
		this.calculateLevel = calculateLevel;
	}
}
