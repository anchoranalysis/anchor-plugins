package org.anchoranalysis.plugin.image.feature.obj.pair;

import java.util.Optional;

import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objmask.ObjMask;

public class CalculateIntersectionInput extends CalculateInputFromDelegateOption<
	FeatureInputSingleObj,
	FeatureInputPairObjs,
	Optional<ObjMask>
> {
	public CalculateIntersectionInput(ResolvedCalculation<Optional<ObjMask>, FeatureInputPairObjs> ccIntersection) {
		super(ccIntersection);
	}

	@Override
	protected Optional<FeatureInputSingleObj> deriveFromDelegate(
		FeatureInputPairObjs input,
		Optional<ObjMask> delegate
	) {
		if (!delegate.isPresent()) {
			return Optional.empty();
		}

		assert(delegate.get().hasPixelsGreaterThan(0));
		assert(delegate.get()!=null);
		
		return Optional.of( 
			new FeatureInputSingleObj( delegate.get(), input.getNrgStackOptional() )		
		);
	}
}
