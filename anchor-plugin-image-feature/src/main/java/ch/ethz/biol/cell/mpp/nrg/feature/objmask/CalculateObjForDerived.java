package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import java.util.Optional;

import org.anchoranalysis.feature.cache.calculation.RslvdCachedCalculation;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.plugin.image.feature.obj.pair.CalculateInputFromDelegateOption;

public class CalculateObjForDerived extends CalculateInputFromDelegateOption<
	FeatureInputSingleObj,
	FeatureInputSingleObj,
	ObjMask
> {
	public CalculateObjForDerived(RslvdCachedCalculation<ObjMask, FeatureInputSingleObj> ccDerived) {
		super(ccDerived);
	}

	@Override
	protected Optional<FeatureInputSingleObj> deriveFromDelegate(FeatureInputSingleObj params, ObjMask delegate) {
		
		if (delegate==null || !delegate.hasPixelsGreaterThan(0)) {
			return Optional.empty();
		}
		
		return Optional.of(
			new FeatureInputSingleObj( delegate, params.getNrgStack() )
		);
	}
}
