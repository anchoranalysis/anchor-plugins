package org.anchoranalysis.plugin.mpp.experiment.feature;

import org.anchoranalysis.feature.session.strategy.replace.CacheAndReuseStrategy;
import org.anchoranalysis.feature.session.strategy.replace.ReplaceStrategy;
import org.anchoranalysis.feature.session.strategy.replace.ReuseSingletonStrategy;
import org.anchoranalysis.feature.session.strategy.replace.bind.BoundReplaceStrategy;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;

/**
 * Strategies for caching used in {@link MergedPairsSession}
 * 
 * @author Owen Feehan
 *
 */
class MergedPairsCachingStrategies {

	private MergedPairsCachingStrategies() {}
	
	/** Cache and re-use inputs */
	public static BoundReplaceStrategy<FeatureInputSingleObj,CacheAndReuseStrategy<FeatureInputSingleObj>> cacheAndReuse() {
		return new BoundReplaceStrategy<>(
			cacheCreator -> new CacheAndReuseStrategy<>(cacheCreator)
		);
	}
	
	/* Don't cache inputs */
	public static BoundReplaceStrategy<FeatureInputStack,? extends ReplaceStrategy<FeatureInputStack>> noCache() {
		return new BoundReplaceStrategy<>(
			cacheCreator -> new ReuseSingletonStrategy<>(cacheCreator)
		);
	}
}
