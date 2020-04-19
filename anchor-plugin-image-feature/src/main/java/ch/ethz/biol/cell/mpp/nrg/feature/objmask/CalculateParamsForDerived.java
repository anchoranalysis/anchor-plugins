package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import java.util.Optional;

import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.session.cache.ICachedCalculationSearch;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.plugin.image.feature.obj.pair.CalculateParamsForIntersection;
import org.anchoranalysis.plugin.image.feature.obj.pair.CalculateParamsFromDelegate;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CalculateParamsForDerived extends CalculateParamsFromDelegate<
	Optional<FeatureObjMaskParams>,
	FeatureObjMaskParams,
	ObjMask
> {
	public static CachedCalculation<Optional<FeatureObjMaskParams>,FeatureObjMaskParams> createFromCache(
		ICachedCalculationSearch<FeatureObjMaskParams> cache,
		CachedCalculation<ObjMask, FeatureObjMaskParams> ccDerived
	) {
		return new CalculateParamsForDerived(
			cache.search(ccDerived)
		);
	}
	
	private CalculateParamsForDerived(CachedCalculation<ObjMask, FeatureObjMaskParams> ccDerived) {
		super(ccDerived);
	}

	@Override
	protected Optional<FeatureObjMaskParams> deriveFromDelegate(FeatureObjMaskParams params, ObjMask delegate) {
		
		if (delegate==null || !delegate.hasPixelsGreaterThan(0)) {
			return Optional.empty();
		}
		
		return Optional.of(
			new FeatureObjMaskParams( delegate, params.getNrgStack() )
		);
	}

	@Override
	public boolean equals(Object other) {
	    if(other instanceof CalculateParamsForIntersection){
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
	public CachedCalculation<Optional<FeatureObjMaskParams>, FeatureObjMaskParams> duplicate() {
		return new CalculateParamsForDerived(duplicateDelegate());
	}
}
