package org.anchoranalysis.plugin.image.feature.obj.pair;

import java.util.Optional;

import org.anchoranalysis.feature.cache.calculation.RslvdCachedCalculation;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.objmask.ObjMask;

public class CalculateParamsForIntersection extends CalculateParamsFromDelegateOption<
	FeatureObjMaskParams,
	FeatureObjMaskPairParams,
	Optional<ObjMask>
> {
	public CalculateParamsForIntersection(RslvdCachedCalculation<Optional<ObjMask>, FeatureObjMaskPairParams> ccIntersection) {
		super(ccIntersection);
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
