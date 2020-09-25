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

package org.anchoranalysis.plugin.annotation.bean.comparison;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.annotation.io.assignment.AssignmentOverlapFromPairs;
import org.anchoranalysis.annotation.io.assignment.generator.AssignmentGeneratorFactory;
import org.anchoranalysis.annotation.io.assignment.generator.ColorPool;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.NamedStacks;
import org.anchoranalysis.io.bean.color.list.ColorListFactory;
import org.anchoranalysis.io.bean.color.list.VeryBright;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.annotation.bean.comparison.assigner.AnnotationComparisonAssigner;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationComparisonInput;
import org.anchoranalysis.plugin.annotation.comparison.IAddAnnotation;
import org.anchoranalysis.plugin.annotation.comparison.ObjectsToCompare;

public class AnnotationComparisonTask<T extends Assignment>
        extends Task<AnnotationComparisonInput<ProvidesStackInput>, SharedState<T>> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String backgroundChannelName = "Image";

    // If a non-empty string it is used to split the descriptive name into groups
    @BeanField @Getter @Setter private String splitDescriptiveNameRegex = "";

    @BeanField @Getter @Setter private int maxSplitGroups = 5;

    @BeanField @Getter @Setter private int numLevelsGrouping = 0;

    @BeanField @Getter @Setter private boolean useMIP = false;

    @BeanField @Getter @Setter private int outlineWidth = 1;

    @BeanField @Getter @Setter private AnnotationComparisonAssigner<T> assigner;

    @BeanField @Getter @Setter private boolean replaceMatchesWithSolids = true;
    // END BEAN PROPERTIES

    private ColorListFactory colorSetGeneratorUnpaired = new VeryBright();

    @Override
    public SharedState<T> beforeAnyJobIsExecuted(
            BoundOutputManagerRouteErrors outputManager,
            ConcurrencyPlan concurrencyPlan,
            ParametersExperiment params)
            throws ExperimentExecutionException {

        try {
            CSVAssignment assignmentCSV =
                    new CSVAssignment(
                            outputManager, "byImage", hasDescriptiveSplit(), maxSplitGroups);
            return new SharedState<>(
                    assignmentCSV, numLevelsGrouping, key -> assigner.groupForKey(key));
        } catch (AnchorIOException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public void doJobOnInput(
            InputBound<AnnotationComparisonInput<ProvidesStackInput>, SharedState<T>> params)
            throws JobExecutionException {

        AnnotationComparisonInput<ProvidesStackInput> input = params.getInputObject();

        // Create the background
        DisplayStack background = createBackground(input);

        // We only do a descriptive split if it's allowed
        SplitString descriptiveSplit = createSplitString(input);

        // Now do whatever comparison is necessary to update the assignment
        Optional<Assignment> assignment =
                compareAndUpdate(
                        input,
                        background,
                        descriptiveSplit,
                        params.context(),
                        params.getSharedState());

        if (!assignment.isPresent()) {
            return;
        }

        writeRGBOutlineStack(
                "rgbOutline", params.getOutputManager(), input, assignment.get(), background);
    }

    private Optional<Assignment> compareAndUpdate(
            AnnotationComparisonInput<ProvidesStackInput> input,
            DisplayStack background,
            SplitString descriptiveSplit,
            BoundIOContext context,
            SharedState<T> sharedState)
            throws JobExecutionException {

        // Get a matching set of groups for this image
        IAddAnnotation<T> addAnnotation = sharedState.groupsForImage(descriptiveSplit);

        Optional<ObjectsToCompare> objectsToCompare =
                ObjectsToCompareFactory.create(
                        input, addAnnotation, background.dimensions(), context);
        return OptionalUtilities.map(
                objectsToCompare,
                objects ->
                        processAcceptedAnnotation(
                                input,
                                background,
                                objects,
                                addAnnotation,
                                sharedState,
                                descriptiveSplit,
                                context));
    }

    private SplitString createSplitString(AnnotationComparisonInput<ProvidesStackInput> input) {
        return hasDescriptiveSplit()
                ? new SplitString(input.descriptiveName(), splitDescriptiveNameRegex)
                : null;
    }

    private DisplayStack createBackground(AnnotationComparisonInput<ProvidesStackInput> inputObject)
            throws JobExecutionException {

        try {
            NamedStacks stackCollection = new NamedStacks();
            inputObject.getInputObject().addToStoreInferNames(stackCollection);
            return DisplayStack.create(stackCollection.getException(backgroundChannelName));

        } catch (CreateException | OperationFailedException e) {
            throw new JobExecutionException(e);
        } catch (NamedProviderGetException e) {
            throw new JobExecutionException(e.summarize());
        }
    }

    private Assignment processAcceptedAnnotation(
            AnnotationComparisonInput<ProvidesStackInput> inputObject,
            DisplayStack background,
            ObjectsToCompare objectsToCompare,
            IAddAnnotation<T> addAnnotation,
            SharedState<T> sharedStateC,
            SplitString descriptiveSplit,
            BoundIOContext context)
            throws JobExecutionException {

        context.getLogReporter().log("Start processAcceptedAnnotation");

        try {
            T assignment =
                    assigner.createAssignment(
                            objectsToCompare, background.dimensions(), useMIP, context);

            addAnnotation.addAcceptedAnnotation(assignment);

            // Statistics row in file
            sharedStateC
                    .getAssignmentCSV()
                    .writeStatisticsForImage(assignment, descriptiveSplit, inputObject);

            return assignment;

        } catch (CreateException | OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    private boolean hasDescriptiveSplit() {
        return splitDescriptiveNameRegex != null && !splitDescriptiveNameRegex.isEmpty();
    }

    private void writeRGBOutlineStack(
            String outputName,
            BoundOutputManagerRouteErrors outputManager,
            AnnotationComparisonInput<ProvidesStackInput> inputObject,
            Assignment assignment,
            DisplayStack background) {

        if (!outputManager.isOutputAllowed(outputName)) {
            return;
        }

        ColorPool colorPool =
                new ColorPool(
                        assignment.numberPaired(),
                        outputManager.getOutputWriteSettings().getDefaultColorSetGenerator(),
                        colorSetGeneratorUnpaired,
                        replaceMatchesWithSolids);

        outputManager
                .getWriterCheckIfAllowed()
                .write(
                        "rgbOutline",
                        () ->
                                AssignmentGeneratorFactory.createAssignmentGenerator(
                                        background,
                                        assignment,
                                        colorPool,
                                        useMIP,
                                        inputObject.getNames(),
                                        outlineWidth,
                                        assigner.moreThanOneObj()));
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(AnnotationComparisonInput.class);
    }

    @Override
    public void afterAllJobsAreExecuted(SharedState<T> sharedState, BoundIOContext context)
            throws ExperimentExecutionException {

        @SuppressWarnings("unchecked")
        SharedState<AssignmentOverlapFromPairs> sharedStateC =
                (SharedState<AssignmentOverlapFromPairs>) sharedState;
        sharedStateC.getAssignmentCSV().end();

        // Write group statistics
        try {
            new CSVComparisonGroup<>(sharedStateC.allGroups())
                    .writeGroupStats(context.getOutputManager());
        } catch (AnchorIOException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }
}
