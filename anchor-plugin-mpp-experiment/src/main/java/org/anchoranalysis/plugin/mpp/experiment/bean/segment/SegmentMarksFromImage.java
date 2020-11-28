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

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.exception.BeanDuplicateException;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.core.identifier.provider.store.LazyEvaluationStore;
import org.anchoranalysis.core.identifier.provider.store.NamedProviderStore;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.value.KeyValueParams;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.core.stack.named.NamedStacks;
import org.anchoranalysis.image.core.stack.time.WrapStackAsTimeSequenceStore;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.io.generator.serialized.XStreamGenerator;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.io.output.BackgroundCreator;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.segment.bean.ExperimentState;
import org.anchoranalysis.mpp.segment.bean.SegmentIntoMarks;

/**
 * Segments an image into a collection of {@link Mark}s.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>marks</td><td>yes</td><td>The segmented outcome as serialized XML using <a href="https://x-stream.github.io/">XStream</a>.</td></tr>
 * <tr><td>outline</td><td>yes</td><td>A RGB image showing <i>outline-colored</i> segmented marks on a background.</td></tr>
 * <tr><td>solid</td><td>no</td><td>A RGB image showing <i>solidly-colored</i> segmented marks on a background.</td></tr>
 * <tr><td rowspan="3"><i>any outputs produced by {@link SegmentIntoMarks}</i> in {@code segment}.</td></tr>
 * <tr><td rowspan="3"><i>inherited from {@link Task}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 */
public class SegmentMarksFromImage extends Task<MultiInput, ExperimentState> {

    private static final String OUTPUT_MARKS = "marks";

    private static final Optional<String> MANIFEST_FUNCTION_SERIALIZED_MARKS =
            Optional.of(OUTPUT_MARKS);

    // START BEAN PROPERTIES
    /** How to perform the segmentation. */
    @BeanField @Getter @Setter private SegmentIntoMarks segment;

    /**
     * If non-empty, the identifier of a key-value-params collection that is passed to the
     * segmentation procedure.
     */
    @BeanField @AllowEmpty @Getter @Setter private String keyValueParamsID = "";
    // END BEAN PROPERTIES

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(MultiInput.class);
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        OutputEnabledMutable outputs =
                super.defaultOutputs()
                        .addEnabledOutputFirst(
                                OUTPUT_MARKS, MarksVisualization.OUTPUT_VISUALIZE_MARKS_OUTLINE);
        outputs.addEnabledOutputs(segment.defaultOutputs());
        return outputs;
    }

    @Override
    public ExperimentState beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<MultiInput> inputs,
            ParametersExperiment params)
            throws ExperimentExecutionException {
        ExperimentState experimentState = segment.createExperimentState();
        experimentState.outputBeforeAnyTasksAreExecuted(outputter);
        return experimentState;
    }

    @Override
    public void doJobOnInput(InputBound<MultiInput, ExperimentState> inputBound)
            throws JobExecutionException {

        MultiInput input = inputBound.getInput();

        try {
            NamedStacks stackCollection = stacksFromInput(input);

            NamedProviderStore<ObjectCollection> objects = objectsFromInput(input);

            Optional<KeyValueParams> paramsCreated = keyValueParamsFromInput(input);

            MarkCollection marks =
                    segment.duplicateBean()
                            .segment(
                                    stackCollection,
                                    objects,
                                    paramsCreated,
                                    inputBound.getContextJob());
            writeVisualization(
                    marks, inputBound.getOutputter(), stackCollection, inputBound.getLogger());

        } catch (SegmentationFailedException e) {
            throw new JobExecutionException("An error occurred segmenting a configuration", e);
        } catch (BeanDuplicateException e) {
            throw new JobExecutionException("An error occurred duplicating the sgmn bean", e);
        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(ExperimentState sharedState, InputOutputContext context)
            throws ExperimentExecutionException {
        sharedState.outputAfterAllTasksAreExecuted(context.getOutputter());
    }

    private NamedStacks stacksFromInput(MultiInput input) throws OperationFailedException {
        NamedStacks stackCollection = new NamedStacks();
        input.stack().addToStore(new WrapStackAsTimeSequenceStore(stackCollection));
        return stackCollection;
    }

    private Optional<KeyValueParams> keyValueParamsFromInput(MultiInput input)
            throws JobExecutionException {
        NamedProviderStore<KeyValueParams> paramsCollection =
                new LazyEvaluationStore<>("keyValueParams");
        try {
            input.keyValueParams().addToStore(paramsCollection);
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

    private NamedProviderStore<ObjectCollection> objectsFromInput(MultiInput input)
            throws OperationFailedException {
        NamedProviderStore<ObjectCollection> objectsStore =
                new LazyEvaluationStore<>("object-collections");
        input.objects().addToStore(objectsStore);
        return objectsStore;
    }

    private void writeVisualization(
            MarkCollection marks, Outputter outputter, NamedStacks stackCollection, Logger logger) {
        outputter
                .writerSelective()
                .write(
                        OUTPUT_MARKS,
                        () -> new XStreamGenerator<Object>(MANIFEST_FUNCTION_SERIALIZED_MARKS),
                        () -> marks);

        try {
            DisplayStack backgroundStack =
                    BackgroundCreator.createBackground(
                            stackCollection, segment.getBackgroundStackName());

            new MarksVisualization(marks, outputter, backgroundStack).write();
        } catch (CreateException e) {
            logger.errorReporter().recordError(SegmentMarksFromImage.class, e);
        }
    }
}
