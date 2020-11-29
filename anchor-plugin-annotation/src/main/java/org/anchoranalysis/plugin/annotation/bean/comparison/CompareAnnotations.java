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

import io.vavr.Tuple2;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.annotation.io.assignment.generator.AssignmentGenerator;
import org.anchoranalysis.annotation.io.assignment.generator.ColorPool;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.color.scheme.ColorScheme;
import org.anchoranalysis.bean.shared.color.scheme.VeryBright;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.core.stack.named.NamedStacks;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.annotation.bean.comparison.assigner.AnnotationComparisonAssigner;
import org.anchoranalysis.plugin.annotation.comparison.AddAnnotation;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationComparisonInput;
import org.anchoranalysis.plugin.annotation.comparison.ObjectsToCompare;

/**
 * Task to compare a set of annotations to a segmentation or another set of annotations.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>byImage</td><td>yes</td><td>a single CSV file showing summary statistics of matching <i>for all images</i>.</td></tr>
 * <tr><td>byGroup</td><td>yes</td><td>a single CSV file showing summary statistics of matching <i>for all groups group of images</i>.</td></tr>
 * <tr><td>outline</td><td>yes</td><td>a file per image showing a colored representation of which annotations matched (or didn't) <i>for each image</i>.</td></tr>
 * <tr><td rowspan="3"><i>outputs from the {@link AnnotationComparisonAssigner} in {@code assign}</i></td></tr>
 * <tr><td rowspan="3"><i>outputs from {@link Task}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 * @param <T>
 */
public class CompareAnnotations<T extends Assignment>
        extends Task<AnnotationComparisonInput<ProvidesStackInput>, SharedState<T>> {

    private static final String OUTPUT_BY_IMAGE = "byImage";

    public static final String OUTPUT_BY_GROUP = "byGroup";

    private static final String OUTPUT_OUTLINE = "outline";

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String backgroundChannelName = "Image";

    // If a non-empty string it is used to split the descriptive name into groups
    @BeanField @Getter @Setter private String splitDescriptiveNameRegex = "";

    @BeanField @Getter @Setter private int maxSplitGroups = 5;

    @BeanField @Getter @Setter private int numberLevelsGrouping = 0;

    @BeanField @Getter @Setter private boolean useMIP = false;

    @BeanField @Getter @Setter private int outlineWidth = 1;

    @BeanField @Getter @Setter private AnnotationComparisonAssigner<T> assigner;

    @BeanField @Getter @Setter private boolean replaceMatchesWithSolids = true;

    @BeanField @Getter @Setter private ColorScheme colorsUnpaired = new VeryBright();
    // END BEAN PROPERTIES

    @Override
    public SharedState<T> beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<AnnotationComparisonInput<ProvidesStackInput>> inputs,
            ParametersExperiment params)
            throws ExperimentExecutionException {

        try {
            CSVAssignment assignmentCSV =
                    new CSVAssignment(
                            outputter, OUTPUT_BY_IMAGE, hasDescriptiveSplit(), maxSplitGroups);
            return new SharedState<>(assignmentCSV, numberLevelsGrouping, assigner::groupForKey);
        } catch (OutputWriteFailedException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public void doJobOnInput(
            InputBound<AnnotationComparisonInput<ProvidesStackInput>, SharedState<T>> params)
            throws JobExecutionException {

        AnnotationComparisonInput<ProvidesStackInput> input = params.getInput();

        // Create the background.
        DisplayStack background = createBackground(input);

        // We only do a descriptive split if it's allowed.
        SplitString descriptiveSplit = createSplitString(input);

        // Now do whatever comparison is necessary to update the assignment.
        Optional<Assignment> assignment =
                compareAndUpdate(
                        input,
                        background,
                        descriptiveSplit,
                        params.getContextJob(),
                        params.getSharedState());

        if (assignment.isPresent()) {
            writeOutlineStack(params.getOutputter(), input, assignment.get(), background);
        }
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(AnnotationComparisonInput.class);
    }

    @Override
    public void afterAllJobsAreExecuted(SharedState<T> sharedState, InputOutputContext context)
            throws ExperimentExecutionException {

        sharedState.getAssignmentCSV().end();

        // Write group statistics
        try {
            new CSVComparisonGroup<>(sharedState.allGroups(), OUTPUT_BY_GROUP)
                    .writeGroupStats(context.getOutputter());
        } catch (OutputWriteFailedException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs()
                .addEnabledOutputFirst(OUTPUT_BY_IMAGE, OUTPUT_BY_GROUP, OUTPUT_OUTLINE);
    }

    private Optional<Assignment> compareAndUpdate(
            AnnotationComparisonInput<ProvidesStackInput> input,
            DisplayStack background,
            SplitString descriptiveSplit,
            InputOutputContext context,
            SharedState<T> sharedState)
            throws JobExecutionException {

        // Get a matching set of groups for this image
        AddAnnotation<T> addAnnotation = sharedState.groupsForImage(descriptiveSplit);

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
                ? new SplitString(input.name(), splitDescriptiveNameRegex)
                : null;
    }

    private DisplayStack createBackground(AnnotationComparisonInput<ProvidesStackInput> input)
            throws JobExecutionException {

        try {
            NamedStacks stacks = new NamedStacks();
            input.getInput().addToStoreInferNames(stacks);
            return DisplayStack.create(stacks.getException(backgroundChannelName));

        } catch (CreateException | OperationFailedException e) {
            throw new JobExecutionException(e);
        } catch (NamedProviderGetException e) {
            throw new JobExecutionException(e.summarize());
        }
    }

    private Assignment processAcceptedAnnotation(
            AnnotationComparisonInput<ProvidesStackInput> input,
            DisplayStack background,
            ObjectsToCompare objectsToCompare,
            AddAnnotation<T> addAnnotation,
            SharedState<T> sharedStateC,
            SplitString descriptiveSplit,
            InputOutputContext context)
            throws JobExecutionException {

        context.getMessageReporter().log("Start processAcceptedAnnotation");

        try {
            T assignment =
                    assigner.createAssignment(
                            objectsToCompare, background.dimensions(), useMIP, context);

            addAnnotation.addAcceptedAnnotation(assignment);

            // Statistics row in file
            sharedStateC
                    .getAssignmentCSV()
                    .writeStatisticsForImage(assignment, descriptiveSplit, input);

            return assignment;

        } catch (CreateException | OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    private boolean hasDescriptiveSplit() {
        return splitDescriptiveNameRegex != null && !splitDescriptiveNameRegex.isEmpty();
    }

    private void writeOutlineStack(
            Outputter outputter,
            AnnotationComparisonInput<ProvidesStackInput> input,
            Assignment assignment,
            DisplayStack background) {

        outputter
                .writerSelective()
                .write(
                        OUTPUT_OUTLINE,
                        () ->
                                createAssignmentGenerator(
                                        background,
                                        outputter.getSettings().getDefaultColors(),
                                        input.getNames()),
                        () -> assignment);
    }

    private AssignmentGenerator createAssignmentGenerator(
            DisplayStack background,
            ColorScheme colorSchemeFromSettings,
            Tuple2<String, String> names) {
        return new AssignmentGenerator(
                background,
                numberPaired -> createColorPool(numberPaired, colorSchemeFromSettings),
                useMIP,
                names,
                assigner.moreThanOneObject(),
                outlineWidth);
    }

    private ColorPool createColorPool(int numberPaired, ColorScheme colorSchemeFromSettings) {
        return new ColorPool(
                numberPaired, colorSchemeFromSettings, colorsUnpaired, replaceMatchesWithSolids);
    }
}
