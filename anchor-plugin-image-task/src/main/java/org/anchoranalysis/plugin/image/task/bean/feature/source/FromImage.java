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

package org.anchoranalysis.plugin.image.task.bean.feature.source;

import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.results.ResultsVector;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.imagefeature.calculator.FeatureCalculatorFromProviderFactory;

/**
 * Each image produces one row of features
 *
 * <p>*
 */
public class FromImage extends SingleRowPerInput<ProvidesStackInput, FeatureInputStack> {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean private StackProvider nrgStackProvider;
    // END BEAN PROPERTIES

    public FromImage() {
        super("image");
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(ProvidesStackInput.class);
    }

    @Override
    public boolean includeGroupInExperiment(boolean groupGeneratorDefined) {
        return groupGeneratorDefined;
    }

    @Override
    protected ResultsVector calcResultsVectorForInputObject(
            ProvidesStackInput inputObject,
            FeatureList<FeatureInputStack> features,
            FeatureNameList featureNames,
            BoundIOContext context)
            throws FeatureCalcException {
        return createCalculator(inputObject, features, context).calc(new FeatureInputStack());
    }

    private FeatureCalculatorMulti<FeatureInputStack> createCalculator(
            ProvidesStackInput inputObject,
            FeatureList<FeatureInputStack> features,
            BoundIOContext context)
            throws FeatureCalcException {
        try {
            FeatureCalculatorFromProviderFactory<FeatureInputStack> featCalc =
                    new FeatureCalculatorFromProviderFactory<>(
                            inputObject, Optional.ofNullable(getNrgStackProvider()), context);
            return featCalc.calculatorForAll(features);
        } catch (OperationFailedException e) {
            throw new FeatureCalcException(e);
        }
    }

    public StackProvider getNrgStackProvider() {
        return nrgStackProvider;
    }

    public void setNrgStackProvider(StackProvider nrgStackProvider) {
        this.nrgStackProvider = nrgStackProvider;
    }
}
