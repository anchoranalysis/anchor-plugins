/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.aggregate;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.annotation.io.bean.strategy.AnnotatorStrategy;
import org.anchoranalysis.annotation.io.input.AnnotationWithStrategy;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;

/**
 * Aggregates many per-image annotations together in form of a CSV file
 *
 * @author Owen Feehan
 */
public class AnnotationAggregateTask<S extends AnnotatorStrategy>
        extends Task<AnnotationWithStrategy<S>, AggregateSharedState> {

    @Override
    public AggregateSharedState beforeAnyJobIsExecuted(
            BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
            throws ExperimentExecutionException {
        try {
            return new AggregateSharedState();
        } catch (CreateException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public void doJobOnInputObject(
            InputBound<AnnotationWithStrategy<S>, AggregateSharedState> params)
            throws JobExecutionException {

        Optional<ImageAnnotation> ann = createFromInputObject(params.getInputObject());
        ann.ifPresent(annotation -> params.getSharedState().getAnnotations().add(annotation));
    }

    @Override
    public void afterAllJobsAreExecuted(AggregateSharedState sharedState, BoundIOContext context)
            throws ExperimentExecutionException {

        context.getOutputManager()
                .getWriterCheckIfAllowed()
                .write("annotationsAgg", () -> createGenerator(sharedState.getAnnotations()));
    }

    private static AnnotationAggregateCSVGenerator createGenerator(
            List<ImageAnnotation> annotations) {
        AnnotationAggregateCSVGenerator generator = new AnnotationAggregateCSVGenerator();
        generator.setIterableElement(annotations);
        return generator;
    }

    private Optional<ImageAnnotation> createFromInputObject(AnnotationWithStrategy<S> inputObject)
            throws JobExecutionException {
        try {
            return inputObject
                    .labelForAggregation()
                    .map(label -> new ImageAnnotation(inputObject.descriptiveName(), label));
        } catch (AnchorIOException exc) {
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
}
