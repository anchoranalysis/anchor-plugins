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

package org.anchoranalysis.plugin.mpp.experiment.bean.cfg;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanDuplicateException;
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
import org.anchoranalysis.image.stack.NamedStackCollection;
import org.anchoranalysis.image.stack.wrap.WrapStackAsTimeSequenceStore;
import org.anchoranalysis.io.generator.serialized.XStreamGenerator;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.io.output.BackgroundCreator;
import org.anchoranalysis.mpp.sgmn.bean.cfg.CfgSgmn;
import org.anchoranalysis.mpp.sgmn.bean.cfg.ExperimentState;
import lombok.Getter;
import lombok.Setter;

public class CfgSgmnTask extends Task<MultiInput, ExperimentState> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private CfgSgmn sgmn = null;

    @BeanField @Getter @Setter private String outputNameOriginal = "original";

    @BeanField @AllowEmpty @Getter @Setter private String keyValueParamsID = "";
    // END BEAN PROPERTIES

    @Override
    public void doJobOnInputObject(InputBound<MultiInput, ExperimentState> params)
            throws JobExecutionException {

        Logger logger = params.getLogger();
        MultiInput inputObject = params.getInputObject();

        assert (logger != null);

        try {
            NamedStackCollection stackCollection = stacksFromInput(inputObject);

            NamedProviderStore<ObjectCollection> objects = objectsFromInput(inputObject, logger);

            Optional<KeyValueParams> keyValueParams = keyValueParamsFromInput(inputObject, logger);

            Cfg cfg =
                    sgmn.duplicateBean()
                            .sgmn(stackCollection, objects, keyValueParams, params.context());
            writeVisualization(cfg, params.getOutputManager(), stackCollection, logger);

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

    private NamedStackCollection stacksFromInput(MultiInput inputObject)
            throws OperationFailedException {
        NamedStackCollection stackCollection = new NamedStackCollection();
        inputObject.stack().addToStore(new WrapStackAsTimeSequenceStore(stackCollection));
        return stackCollection;
    }

    private Optional<KeyValueParams> keyValueParamsFromInput(MultiInput inputObject, Logger logger)
            throws JobExecutionException {
        NamedProviderStore<KeyValueParams> paramsCollection =
                new LazyEvaluationStore<>(logger, "keyValueParams");
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

    private NamedProviderStore<ObjectCollection> objectsFromInput(
            MultiInput inputObject, Logger logger) throws OperationFailedException {
        NamedProviderStore<ObjectCollection> objectsStore =
                new LazyEvaluationStore<>(logger, "object-colelctions");
        inputObject.objects().addToStore(objectsStore);
        return objectsStore;
    }

    private void writeVisualization(
            Cfg cfg,
            BoundOutputManagerRouteErrors outputManager,
            NamedStackCollection stackCollection,
            Logger logger) {
        outputManager
                .getWriterCheckIfAllowed()
                .write("cfg", () -> new XStreamGenerator<Object>(cfg, Optional.of("cfg")));

        try {
            DisplayStack backgroundStack =
                    BackgroundCreator.createBackground(
                            stackCollection, sgmn.getBackgroundStackName());

            CfgVisualization.write(cfg, outputManager, backgroundStack);
        } catch (OperationFailedException | CreateException e) {
            logger.errorReporter().recordError(CfgSgmnTask.class, e);
        }
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public ExperimentState beforeAnyJobIsExecuted(
            BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
            throws ExperimentExecutionException {
        ExperimentState es = sgmn.createExperimentState();
        es.outputBeforeAnyTasksAreExecuted(outputManager);
        return es;
    }

    @Override
    public void afterAllJobsAreExecuted(ExperimentState sharedState, BoundIOContext context)
            throws ExperimentExecutionException {
        sharedState.outputAfterAllTasksAreExecuted(context.getOutputManager());
    }
}
