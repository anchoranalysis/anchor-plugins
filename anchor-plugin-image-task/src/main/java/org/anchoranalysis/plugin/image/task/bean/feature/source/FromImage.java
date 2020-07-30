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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calc.NamedFeatureCalculationException;
import org.anchoranalysis.feature.calc.results.ResultsVector;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.bean.thumbnail.stack.ScaleToSize;
import org.anchoranalysis.plugin.image.task.bean.thumbnail.stack.ThumbnailFromStack;
import org.anchoranalysis.plugin.image.task.feature.InputProcessContext;
import org.anchoranalysis.plugin.image.task.feature.ResultsVectorWithThumbnail;
import org.anchoranalysis.plugin.image.task.imagefeature.calculator.FeatureCalculatorFromProvider;
import lombok.Getter;
import lombok.Setter;

/**
 * Each image produces one row of features
 *
 * <p>*
 */
public class FromImage extends SingleRowPerInput<ProvidesStackInput, FeatureInputStack> {

    // START BEAN PROPERTIES
    /** Optionally defines a nrg-stack for feature calculation (if not set, the nrg-stack is considered to be the input stacks) */
    @BeanField @OptionalBean @Getter @Setter private StackProvider nrgStackProvider;
    
    /** Method to generate a thumbnail for images */
    @BeanField @Getter @Setter private ThumbnailFromStack thumbnail = new ScaleToSize();
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
    protected ResultsVectorWithThumbnail calculateResultsForInput(
            ProvidesStackInput inputObject,
            InputProcessContext<FeatureList<FeatureInputStack>> context)
        throws NamedFeatureCalculationException {
        
        FeatureCalculatorFromProvider<FeatureInputStack> factory = createCalculator(inputObject, context.getContext());
        
        // Calculate the results for the current stack
        ResultsVector results = calculateResults(factory, context.getRowSource());
        
        thumbnail.start();
        
        try {
            return new ResultsVectorWithThumbnail(
                  results,
                  extractThumbnail(factory.getNrgStack(), context.isThumbnails())
            );
        } catch (CreateException e) {
            throw new NamedFeatureCalculationException(e);
        }
    }
    
    private ResultsVector calculateResults(FeatureCalculatorFromProvider<FeatureInputStack> factory, FeatureList<FeatureInputStack> features) throws NamedFeatureCalculationException {
        try {
            return factory.calculatorForAll(features).calc(new FeatureInputStack());
        } catch (InitException e) {
            throw new NamedFeatureCalculationException(e);
        }
    }

    private Optional<DisplayStack> extractThumbnail(NRGStackWithParams nrgStack, boolean thumbnails) throws CreateException {
        if (thumbnails) {
            return Optional.of( thumbnail.thumbnailFor(nrgStack.getNrgStack().asStack()) );
        } else {
            return Optional.empty();
        }
    }
    
    private FeatureCalculatorFromProvider<FeatureInputStack> createCalculator(
            ProvidesStackInput inputObject,
            BoundIOContext context)
            throws NamedFeatureCalculationException {
        try {
            return new FeatureCalculatorFromProvider<>(
                            inputObject, Optional.ofNullable(getNrgStackProvider()), context);
        } catch (OperationFailedException e) {
            throw new NamedFeatureCalculationException(e);
        }
    }
}
