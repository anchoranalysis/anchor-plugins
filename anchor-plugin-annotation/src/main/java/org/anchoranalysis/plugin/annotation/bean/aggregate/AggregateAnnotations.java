/*-
 * #%L
 * anchor-plugin-annotation
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

package org.anchoranalysis.plugin.annotation.bean.aggregate;

import java.util.Optional;
import org.anchoranalysis.annotation.io.AnnotationWithStrategy;
import org.anchoranalysis.annotation.io.bean.AnnotatorStrategy;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;

/**
 * Aggregates many per-image annotations together in form of a CSV file.
 *
 * @author Owen Feehan
 */
public class AggregateAnnotations<S extends AnnotatorStrategy>
        extends Task<AnnotationWithStrategy<S>, AggregateSharedState> {

    @Override
    public AggregateSharedState beforeAnyJobIsExecuted(
            Outputter outputter, ConcurrencyPlan concurrencyPlan, ParametersExperiment params)
            throws ExperimentExecutionException {
        try {
            return new AggregateSharedState();
        } catch (CreateException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public void doJobOnInput(InputBound<AnnotationWithStrategy<S>, AggregateSharedState> params)
            throws JobExecutionException {

        Optional<ImageAnnotation> ann = createFromInput(params.getInput());
        ann.ifPresent(annotation -> params.getSharedState().getAnnotations().add(annotation));
    }

    @Override
    public void afterAllJobsAreExecuted(
            AggregateSharedState sharedState, InputOutputContext context)
            throws ExperimentExecutionException {

        context.getOutputter()
                .writerSelective()
                .write(
                        "annotationsAgg",
                        AnnotationAggregateCSVGenerator::new,
                        sharedState::getAnnotations);
    }

    private Optional<ImageAnnotation> createFromInput(AnnotationWithStrategy<S> input)
            throws JobExecutionException {
        try {
            return input.labelForAggregation()
                    .map(label -> new ImageAnnotation(input.name(), label));
        } catch (InputReadFailedException exc) {
            throw new JobExecutionException(exc);
        }
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(AnnotationWithStrategy.class);
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        assert (false);
        return super.defaultOutputs();
    }
}
