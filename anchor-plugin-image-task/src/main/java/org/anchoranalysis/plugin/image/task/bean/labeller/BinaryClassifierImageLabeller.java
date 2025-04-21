/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean.labeller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonEmpty;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.feature.input.FeatureInputStack;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.feature.calculator.FeatureCalculatorFromProvider;

/** Labels images using a binary classifier. */
public class BinaryClassifierImageLabeller extends BinaryOutcomeImageLabeller {

    // START BEAN PROPERTIES
    /** Provider for the classifier feature list. */
    @BeanField @SkipInit @Getter @Setter
    private FeatureListProvider<FeatureInputStack> classifierProvider;

    /** List of named feature list providers for additional features. */
    @BeanField @NonEmpty @Getter @Setter
    private List<NamedBean<FeatureListProvider<FeatureInputStack>>> listFeatures =
            new ArrayList<>(0);

    /** Provider for the energy stack. */
    @BeanField @Getter @Setter private StackProvider stackEnergy;

    // END BEAN PROPERTIES

    /**
     * Determines the label for a given input image.
     *
     * @param sharedState shared state (unused in this implementation)
     * @param input the input image provider
     * @param context the input/output context
     * @return the label ("positive" or "negative") based on the classification result
     * @throws OperationFailedException if the labeling operation fails
     */
    @Override
    public String labelFor(
            NoSharedState sharedState, ProvidesStackInput input, InputOutputContext context)
            throws OperationFailedException {

        try {
            FeatureCalculatorFromProvider<FeatureInputStack> featureCalculator =
                    new FeatureCalculatorFromProvider<>(
                            input, Optional.of(getStackEnergy()), context);

            double classificationValue =
                    featureCalculator
                            .calculatorSingleFromProvider(classifierProvider, "classifierProvider")
                            .calculate(new FeatureInputStack());

            context.getMessageReporter()
                    .logFormatted("Classification value = %f", classificationValue);

            // If classification val is >= 0, then it is POSITIVE
            // If classification val is < 0, then it is NEGATIVE
            return classificationString(classificationValue >= 0);

        } catch (FeatureCalculationException e) {
            throw new OperationFailedException(e);
        }
    }
}
