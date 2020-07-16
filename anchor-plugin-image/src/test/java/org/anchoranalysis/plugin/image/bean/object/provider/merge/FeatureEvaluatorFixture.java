/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import java.nio.file.Path;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluatorNrgStack;
import org.anchoranalysis.plugin.image.test.ProviderFixture;

class FeatureEvaluatorFixture {

    private FeatureEvaluatorFixture() {}

    public static <T extends FeatureInput> FeatureEvaluatorNrgStack<T> createNrg(
            Feature<T> feature, Logger logger, Path modelDirectory) throws CreateException {
        FeatureEvaluatorNrgStack<T> eval = new FeatureEvaluatorNrgStack<>();
        eval.setFeatureProvider(ProviderFixture.providerFor(feature));
        try {
            eval.init(SharedFeaturesInitParams.create(logger, modelDirectory), logger);
        } catch (InitException e) {
            throw new CreateException(e);
        }
        return eval;
    }
}
