package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import java.util.Optional;

import org.anchoranalysis.feature.cache.calculation.RslvdCachedCalculation;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.plugin.image.feature.obj.pair.CalculateParamsFromDelegateOption;

public class CalculateParamsForDerived extends CalculateParamsFromDelegateOption<
	FeatureObjMaskParams,
	FeatureObjMaskParams,
	ObjMask
> {
	public CalculateParamsForDerived(RslvdCachedCalculation<ObjMask, FeatureObjMaskParams> ccDerived) {
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
}
