package org.anchoranalysis.plugin.image.feature.obj.pair;

import java.util.Optional;

import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.session.cache.ICachedCalculationSearch;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CalculateParamsForIntersection extends CalculateParamsFromDelegate<
	Optional<FeatureObjMaskParams>,
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
		
	private CalculateParamsForIntersection(CachedCalculation<Optional<ObjMask>, FeatureObjMaskPairParams> ccIntersection) {
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
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculateParamsForIntersection){
	        return true;
	    } else{
	        return false;
	    }
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().toHashCode();
	}

	@Override
	public CachedCalculation<Optional<FeatureObjMaskParams>, FeatureObjMaskPairParams> duplicate() {
		return new CalculateParamsForIntersection(duplicateDelegate());
	}

}
