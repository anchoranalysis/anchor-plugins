/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.mpp.experiment.bean.segment;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.name.store.LazyEvaluationStore;
import org.anchoranalysis.core.name.store.NamedProviderStore;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.NamedStacks;
import org.anchoranalysis.image.stack.wrap.WrapStackAsTimeSequenceStore;
import org.anchoranalysis.io.generator.serialized.XStreamGenerator;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.io.output.BackgroundCreator;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.segment.bean.ExperimentState;
import org.anchoranalysis.mpp.segment.bean.SegmentIntoMarks;

public class SegmentIntoMarksTask extends Task<MultiInput, ExperimentState> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private SegmentIntoMarks segment = null;

    @BeanField @Getter @Setter private String outputNameOriginal = "original";

    @BeanField @AllowEmpty @Getter @Setter private String keyValueParamsID = "";
    // END BEAN PROPERTIES

    @Override
    public void doJobOnInputObject(InputBound<MultiInput, ExperimentState> params)
            throws JobExecutionException {

        MultiInput inputObject = params.getInputObject();

        try {
            NamedStacks stackCollection = stacksFromInput(inputObject);

            NamedProviderStore<ObjectCollection> objects = objectsFromInput(inputObject);

            Optional<KeyValueParams> paramsCreated = keyValueParamsFromInput(inputObject);

            MarkCollection marks =
                    segment.duplicateBean()
                            .segment(stackCollection, objects, paramsCreated, params.context());
            writeVisualization(
                    marks, params.getOutputManager(), stackCollection, params.getLogger());

        } catch (SegmentationFailedException e) {
            throw new JobExecutionException("An error occurred segmenting a configuration", e);
        } catch (BeanDuplicateException e) {
            throw new JobExecutionException("An error occurred duplicating the sgmn bean", e);
        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(MultiInput.class);
    }

    private NamedStacks stacksFromInput(MultiInput inputObject) throws OperationFailedException {
        NamedStacks stackCollection = new NamedStacks();
        inputObject.stack().addToStore(new WrapStackAsTimeSequenceStore(stackCollection));
        return stackCollection;
    }

    private Optional<KeyValueParams> keyValueParamsFromInput(MultiInput inputObject)
            throws JobExecutionException {
        NamedProviderStore<KeyValueParams> paramsCollection =
                new LazyEvaluationStore<>("keyValueParams");
        try {
            inputObject.keyValueParams().addToStore(paramsCollection);
        } catch (OperationFailedException e1) {
            throw new JobExecutionException("Cannot retrieve key-value-params from input-object");
        }

        // We select a particular key value params to send as output
        try {
            if (!keyValueParamsID.isEmpty()) {
                return Optional.of(paramsCollection.getException(keyValueParamsID));
            } else {
                return Optional.empty();
            }

        } catch (NamedProviderGetException e) {
            throw new JobExecutionException("Cannot retrieve key-values-params", e.summarize());
        }
    }

    private NamedProviderStore<ObjectCollection> objectsFromInput(MultiInput inputObject)
            throws OperationFailedException {
        NamedProviderStore<ObjectCollection> objectsStore =
                new LazyEvaluationStore<>("object-colelctions");
        inputObject.objects().addToStore(objectsStore);
        return objectsStore;
    }

    private void writeVisualization(
            MarkCollection marks,
            BoundOutputManagerRouteErrors outputManager,
            NamedStacks stackCollection,
            Logger logger) {
        outputManager
                .getWriterCheckIfAllowed()
                .write("marks", () -> new XStreamGenerator<Object>(marks, Optional.of("marks")));

        try {
            DisplayStack backgroundStack =
                    BackgroundCreator.createBackground(
                            stackCollection, segment.getBackgroundStackName());

            MarksVisualization.write(marks, outputManager, backgroundStack);
        } catch (OperationFailedException | CreateException e) {
            logger.errorReporter().recordError(SegmentIntoMarksTask.class, e);
        }
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public ExperimentState beforeAnyJobIsExecuted(
            BoundOutputManagerRouteErrors outputManager,
            ConcurrencyPlan concurrencyPlan,
            ParametersExperiment params)
            throws ExperimentExecutionException {
        ExperimentState experimentState = segment.createExperimentState();
        experimentState.outputBeforeAnyTasksAreExecuted(outputManager);
        return experimentState;
    }

    @Override
    public void afterAllJobsAreExecuted(ExperimentState sharedState, BoundIOContext context)
            throws ExperimentExecutionException {
        sharedState.outputAfterAllTasksAreExecuted(context.getOutputManager());
    }
}
