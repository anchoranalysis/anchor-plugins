package ch.ethz.biol.cell.mpp.nrg.feature.objmaskpairmerged;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objmask.ObjMask;

/**
 * Executes a feature
 * 
 * @author Owen Feehan
 *
 */
public class FromSecond extends FromExisting {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final ChildCacheName CACHE_NAME = new ChildCacheName(FromSecond.class);
	
	public FromSecond() {
		// NOTHING TO DO
	}
	
	public FromSecond(Feature<FeatureInputSingleObj> item) {
		super(item);
	}
	
	@Override
	protected ObjMask selectObjMask(FeatureInputPairObjs params) {
		return params.getRight();
	}

	@Override
	public ChildCacheName cacheNameToUse() {
		return CACHE_NAME;
	}
}
