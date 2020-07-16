/* (C)2020 */
package org.anchoranalysis.plugin.image.task.imagefeature.calculator;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;

class ExtractFromProvider {

    private ExtractFromProvider() {}

    public static NRGStackWithParams extractStack(
            StackProvider nrgStackProvider, ImageInitParams initParams, Logger logger)
            throws OperationFailedException {

        try {
            // Extract the NRG stack
            StackProvider nrgStackProviderLoc = nrgStackProvider.duplicateBean();
            nrgStackProviderLoc.initRecursive(initParams, logger);

            return new NRGStackWithParams(nrgStackProviderLoc.create());
        } catch (InitException | CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    /** Creates and initializes a single-feature that is provided via a featureProvider */
    public static <T extends FeatureInput> Feature<T> extractFeature(
            FeatureListProvider<T> featureProvider,
            String featureProviderName,
            SharedFeaturesInitParams initParams,
            Logger logger)
            throws FeatureCalcException {

        try {
            featureProvider.initRecursive(initParams, logger);

            FeatureList<T> fl = featureProvider.create();
            if (fl.size() != 1) {
                throw new FeatureCalcException(
                        String.format(
                                "%s must return exactly one feature from its list. It currently returns %d",
                                featureProviderName, fl.size()));
            }
            return fl.get(0);
        } catch (CreateException | InitException e) {
            throw new FeatureCalcException(e);
        }
    }
}
