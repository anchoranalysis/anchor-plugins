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

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.annotation.io.AnnotationWithStrategy;
import org.anchoranalysis.annotation.io.bean.AnnotatorStrategy;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;

/**
 * Aggregates many per-image annotations together in form of a CSV file.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>{@value #OUTPUT_AGGREGATED}</td><td>yes</td><td>a CSV file with each image and corresponding image-label.</td></tr>
 * <tr><td rowspan="3"><i>outputs from {@link Task}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 * @param <S> the type of {@link AnnotatorStrategy} used for annotation
 */
public class AggregateAnnotations<S extends AnnotatorStrategy>
        extends Task<AnnotationWithStrategy<S>, AggregateSharedState> {

    /** The name of the output for the aggregated CSV file. */
    private static final String OUTPUT_AGGREGATED = "aggregated";

    @Override
    public AggregateSharedState beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<AnnotationWithStrategy<S>> inputs,
            ParametersExperiment parameters)
            throws ExperimentExecutionException {
        try {
            return new AggregateSharedState();
        } catch (CreateException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public void doJobOnInput(InputBound<AnnotationWithStrategy<S>, AggregateSharedState> input)
            throws JobExecutionException {

        Optional<ImageAnnotation> annotation =
                createFromInput(input.getInput(), input.getContextJob().operationContext());
        annotation.ifPresent(
                annotationToAdd -> input.getSharedState().getAnnotations().add(annotationToAdd));
    }

    @Override
    public void afterAllJobsAreExecuted(
            AggregateSharedState sharedState, InputOutputContext context)
            throws ExperimentExecutionException {

        context.getOutputter()
                .writerSelective()
                .write(
                        OUTPUT_AGGREGATED,
                        AnnotationAggregateCSVGenerator::new,
                        sharedState::getAnnotations);
    }

    private Optional<ImageAnnotation> createFromInput(
            AnnotationWithStrategy<S> input, OperationContext context)
            throws JobExecutionException {
        try {
            return input.label(context)
                    .map(label -> new ImageAnnotation(input.identifier(), label));
        } catch (OperationFailedException exc) {
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
        return super.defaultOutputs().addEnabledOutputFirst(OUTPUT_AGGREGATED);
    }
}
