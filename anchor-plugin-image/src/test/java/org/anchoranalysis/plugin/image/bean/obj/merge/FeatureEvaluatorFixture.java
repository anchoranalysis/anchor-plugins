package org.anchoranalysis.plugin.image.bean.obj.merge;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluatorNrgStack;
import org.anchoranalysis.plugin.image.test.ProviderFixture;

class FeatureEvaluatorFixture {
	
	private FeatureEvaluatorFixture() {}
	
	public static <T extends FeatureInput> FeatureEvaluatorNrgStack<T> createNrg( Feature<T> feature, LogErrorReporter logger ) throws CreateException {
		FeatureEvaluatorNrgStack<T> eval = new FeatureEvaluatorNrgStack<>();
		eval.setFeatureProvider(
			ProviderFixture.providerFor( feature )
		);
		try {
			eval.init(  SharedFeaturesInitParams.create(logger), logger );
		} catch (InitException e) {
			throw new CreateException(e);
		}
		return eval;
	}
}