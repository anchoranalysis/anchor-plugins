package org.anchoranalysis.plugin.image.feature.obj.pair;

import java.util.Optional;

import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.cachedcalculation.RslvdCachedCalculation;
import org.anchoranalysis.feature.session.cache.ICachedCalculationSearch;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.objmask.ObjMask;

public class CalculateParamsForIntersection extends CalculateParamsFromDelegateOption<
	FeatureObjMaskParams,
	FeatureObjMaskPairParams,
	Optional<ObjMask>
> {
	
	public static CachedCalculation<Optional<FeatureObjMaskParams>,FeatureObjMaskPairParams> createFromCache(
		ICachedCalculationSearch<FeatureObjMaskPairParams> cache,
		CachedCalculation<Optional<ObjMask>, FeatureObjMaskPairParams> ccIntersection
	) {
		return new CalculateParamsForIntersection(
			cache.search(ccIntersection)
		);
	}
		
	private CalculateParamsForIntersection(RslvdCachedCalculation<Optional<ObjMask>, FeatureObjMaskPairParams> ccIntersection) {
		super(ccIntersection, CalculateParamsForIntersection.class);
	}

	@Override
	protected Optional<FeatureObjMaskParams> deriveFromDelegate(
		FeatureObjMaskPairParams params,
		Optional<ObjMask> delegate
	) {
		
		if (!delegate.isPresent()) {
			return Optional.empty();
		}

		assert(delegate.get().hasPixelsGreaterThan(0));
		assert(delegate.get()!=null);
		
		return Optional.of( 
			new FeatureObjMaskParams( delegate.get(), params.getNrgStack() )		
		);
	}
}
