/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.labeller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonEmpty;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.imagefeature.calculator.FeatureCalculatorFromProviderFactory;

public class BinaryClassifierImageLabeller extends BinaryOutcomeImageLabeller {

    // START BEAN PROPERTIES
    @BeanField @SkipInit private FeatureListProvider<FeatureInputStack> classifierProvider;

    @BeanField @NonEmpty
    private List<NamedBean<FeatureListProvider<FeatureInputStack>>> listFeatures =
            new ArrayList<>();

    @BeanField private StackProvider nrgStackProvider;
    // END BEAN PROPERTIES

    @Override
    public String labelFor(
            NoSharedState sharedState, ProvidesStackInput input, BoundIOContext context)
            throws OperationFailedException {

        try {
            FeatureCalculatorFromProviderFactory<FeatureInputStack> featureCalculator =
                    new FeatureCalculatorFromProviderFactory<>(
                            input, Optional.of(getNrgStackProvider()), context);

            double classificationVal =
                    featureCalculator
                            .calculatorSingleFromProvider(classifierProvider, "classifierProvider")
                            .calc(new FeatureInputStack());

            context.getLogReporter().logFormatted("Classification value = %f", classificationVal);

            // If classification val is >= 0, then it is POSITIVE
            // If classification val is < 0, then it is NEGATIVE
            boolean classificationPositive = classificationVal >= 0;

            return classificationString(classificationPositive);

        } catch (FeatureCalcException e) {
            throw new OperationFailedException(e);
        }
    }

    public FeatureListProvider<FeatureInputStack> getClassifierProvider() {
        return classifierProvider;
    }

    public void setClassifierProvider(FeatureListProvider<FeatureInputStack> classifierProvider) {
        this.classifierProvider = classifierProvider;
    }

    public List<NamedBean<FeatureListProvider<FeatureInputStack>>> getListFeatures() {
        return listFeatures;
    }

    public void setListFeatures(
            List<NamedBean<FeatureListProvider<FeatureInputStack>>> listFeatures) {
        this.listFeatures = listFeatures;
    }

    public StackProvider getNrgStackProvider() {
        return nrgStackProvider;
    }

    public void setNrgStackProvider(StackProvider nrgStackProvider) {
        this.nrgStackProvider = nrgStackProvider;
    }
}
