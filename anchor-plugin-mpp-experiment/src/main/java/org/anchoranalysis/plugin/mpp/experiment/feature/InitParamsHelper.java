package org.anchoranalysis.plugin.mpp.experiment.feature;

import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.feature.init.FeatureInitParamsImageInit;
import org.anchoranalysis.image.init.ImageInitParams;

class InitParamsHelper {
	
	public static FeatureInitParams createInitParams( ImageInitParams so, NRGStack nrgStack, KeyValueParams keyValueParams ) {
		FeatureInitParams params;
		if (so!=null) {
			params = new FeatureInitParamsImageInit( so );
			params.setKeyValueParams(keyValueParams);
		} else {
			params = new FeatureInitParams( keyValueParams );
		}
		params.setNrgStack(nrgStack);
		return params;
	}
}
