/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.GreaterThanEqualToBean;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListFactory;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.input.FeatureInputEnergy;
import org.anchoranalysis.feature.input.FeatureInputNull;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingleChangeInput;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingleFromMulti;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluatorWithEnergy;
import org.anchoranalysis.image.feature.evaluator.PayloadCalculator;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.session.merged.MergedPairsFeatures;
import org.anchoranalysis.image.feature.session.merged.PairsTableCalculator;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.object.merge.priority.AssignPriority;
import org.anchoranalysis.plugin.image.object.merge.priority.AssignPriorityFromPair;

/**
 * Merges objects if a <emph>pair</emph> feature value satisfies a condition
 *
 * <p>The pair feature is calculated on all combinations of any two neighboring objects (but only
 * once for each pair, unidirectionally).
 *
 * <p>The merges occur in order of the maximum increase offered (if the value exceeds the
 * threshold), and the algorithm recursively merge until all possible merges are complete.
 */
public class MergePairs extends MergeWithFeature {

    // START BEAN PROPERTIES
    /** Allows merge only if the feature-value satisfies a relation to this threshold */
    @BeanField @Getter @Setter private FeatureEvaluator<FeatureInputNull> featureEvaluatorThreshold;

    /** Relation to threshold */
    @BeanField @Getter @Setter private RelationBean relation = new GreaterThanEqualToBean();

    @BeanField @Getter @Setter
    private FeatureEvaluatorWithEnergy<FeatureInputPairObjects> featureEvaluatorMerge;
    // END BEAN PROPERTIES

    @Override
    protected PayloadCalculator createPayloadCalculator() {
        // We don't care about evaluating single-features
        return (ObjectMask objectMask) -> 0;
    }

    @Override
    protected AssignPriority createPrioritizer() throws OperationFailedException {
        try {
            double threshold =
                    featureEvaluatorThreshold
                            .createAndStartSession()
                            .calculate(FeatureInputNull.instance());

            return new AssignPriorityFromPair(
                    createCalculatorForPairs(), threshold, relation.create());

        } catch (FeatureCalculationException | CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    protected boolean isPlayloadUsed() {
        return true;
    }

    private FeatureCalculatorSingle<FeatureInputPairObjects> createCalculatorForPairs()
            throws CreateException {
        try {
            Optional<EnergyStack> energyStack = featureEvaluatorMerge.energyStack();

            FeatureList<FeatureInputPairObjects> features = FeatureListFactory.from(
                    featureEvaluatorMerge.getFeature());
            
            PairsTableCalculator session =
                    new PairsTableCalculator(
                            new MergedPairsFeatures(features));
            session.start(getInitializationParameters(), energyStack, getLogger());

            return maybeWrapWithEnergyStack(new FeatureCalculatorSingleFromMulti<>(session), energyStack);

        } catch (OperationFailedException | InitException e) {
            throw new CreateException(e);
        }
    }

    private static <T extends FeatureInputEnergy> FeatureCalculatorSingle<T> maybeWrapWithEnergyStack(
            FeatureCalculatorSingle<T> calculator, Optional<EnergyStack> energyStack) {
        if (energyStack.isPresent()) {
            return new FeatureCalculatorSingleChangeInput<>(
                    calculator, input -> input.setEnergyStack(energyStack));
        } else {
            return calculator;
        }
    }
}
