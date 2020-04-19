package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import java.util.Optional;

import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.cachedcalculation.RslvdCachedCalculation;
import org.anchoranalysis.feature.session.cache.ICachedCalculationSearch;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.plugin.image.feature.obj.pair.CalculateParamsFromDelegateOption;

public class CalculateParamsForDerived extends CalculateParamsFromDelegateOption<
	FeatureObjMaskParams,
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
	
	private CalculateParamsForDerived(RslvdCachedCalculation<ObjMask, FeatureObjMaskParams> ccDerived) {
		super(ccDerived, CalculateParamsForDerived.class);
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
}
