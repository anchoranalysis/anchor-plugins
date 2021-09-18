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

package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorFriendlyCheckedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.io.InitializationContext;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.voxel.object.IntersectingObjects;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.io.manifest.ManifestDirectoryDescription;
import org.anchoranalysis.io.manifest.sequencetype.StringsWithoutOrder;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.enabled.single.SingleLevelOutputEnabled;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.io.output.writer.ElementWriterSupplier;
import org.anchoranalysis.mpp.init.MarksInitialization;
import org.anchoranalysis.mpp.io.input.MarksInitializationFactory;
import org.anchoranalysis.plugin.mpp.experiment.bean.objects.columndefinition.ColumnDefinition;
import org.anchoranalysis.plugin.mpp.experiment.objects.FromCSVInput;
import org.anchoranalysis.plugin.mpp.experiment.objects.FromCSVSharedState;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.CSVRow;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.IndexedCSVRows;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.MapGroupToRow;

/**
 * Given a CSV with a point representing one or more object-masks, these object-masks are extracted
 * from a segmentation.
 *
 * <p>The CSV file-path should be the same for all rasters passed as input. It gets cached between
 * the threads.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>outline</td><td>yes</td><td>RGB image displaying the outline of the extracted object in a cropped region around the objects.</td></tr>
 * <tr><td>unique_point</td><td>no</td><td>A XML file that stores a unique point on each extracted object (uniqueness only guaranteed if no objects overlap).</td></tr>
 * <tr><td rowspan="3"><i>inherited from {@link Task}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 */
public class ExportObjectsFromCSV extends ExportObjectsBase<FromCSVInput, FromCSVSharedState> {

    private static final String GROUP_SUBDIRECTORY = "groups";

    private static final ManifestDirectoryDescription GROUP_MANIFEST_DESCRIPTION =
            new ManifestDirectoryDescription(
                    "groupedDirectory", "groupedOutline", new StringsWithoutOrder());

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter
    private DerivePath idGenerator; // Translates an input file name to a unique ID

    /** The stack that is the background for our objects */
    @BeanField @Getter @Setter private StackProvider stack;

    /** Column definition for the CSV files */
    @BeanField @Getter @Setter private ColumnDefinition columnDefinition;
    // END BEAN PROPERTIES

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(FromCSVInput.class);
    }

    @Override
    public FromCSVSharedState beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<FromCSVInput> inputs,
            ParametersExperiment params)
            throws ExperimentExecutionException {
        return new FromCSVSharedState();
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public void doJobOnInput(InputBound<FromCSVInput, FromCSVSharedState> inputBound)
            throws JobExecutionException {

        if (!OutlineOutputHelper.anyOutputEnabled(
                inputBound.getContextJob().getOutputter().outputsEnabled())) {
            // If no outputs are enabled, there's nothing to do, so exit early
        }

        FromCSVInput input = inputBound.getInput();
        InputOutputContext groupContext =
                inputBound
                        .getContextJob()
                        .subdirectory(GROUP_SUBDIRECTORY, GROUP_MANIFEST_DESCRIPTION, false);
        try {
            IndexedCSVRows groupedRows =
                    inputBound
                            .getSharedState()
                            .getIndexedRowsOrCreate(input.getCsvFilePath(), columnDefinition);

            // We look for rows that match our File ID
            String fileIdentifier =
                    IdentifierHelper.identifierForPathAsString(
                            idGenerator, input.pathForBinding(), groupContext.isDebugEnabled());
            MapGroupToRow mapGroup = groupedRows.get(fileIdentifier);

            if (mapGroup != null) {
                InitializationContext initContext =
                        new InitializationContext(
                                groupContext, inputBound.getTaskArguments().getSize());
                MarksInitialization initialization =
                        MarksInitializationFactory.create(
                                Optional.of(input), initContext, Optional.empty());
                processFileWithMap(
                        initialization.image(),
                        mapGroup,
                        groupedRows.groupNameSet(),
                        groupContext,
                        inputBound.getContextJob().getOutputter().outputsEnabled());
            } else {
                groupContext
                        .getMessageReporter()
                        .logFormatted(
                                "No matching rows in CSV file for id '%s' from %s",
                                fileIdentifier,
                                input.pathForBinding()
                                        .map(path -> "'" + path.toString() + "'")
                                        .orElse("<no binding path>"));
            }

        } catch (AnchorFriendlyCheckedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(FromCSVSharedState sharedState, InputOutputContext context)
            throws ExperimentExecutionException {
        // NOTHING TO DO
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs().addEnabledOutputFirst(OutlineOutputHelper.OUTPUT_OUTLINE);
    }

    private void processFileWithMap(
            ImageInitialization initialization,
            MapGroupToRow mapGroup,
            Set<String> groupNameSet,
            InputOutputContext groupContext,
            SingleLevelOutputEnabled outputRules)
            throws OperationFailedException {

        try {
            IntersectingObjects<ObjectMask> objects =
                    IntersectingObjects.create(inputs(initialization, groupContext.getLogger()));

            // Create a writer with output-naming rules not from the group-context, but the
            // top-level experiment context.
            ElementWriterSupplier<Collection<CSVRow>> writer =
                    () ->
                            OutlineOutputHelper.createGenerator(
                                    objects,
                                    () ->
                                            createBackgroundStack(
                                                    initialization, groupContext.getLogger()),
                                    getPadding(),
                                    outputRules);

            // Loop through each group, and output a series of TIFFs, where set of objects is shown
            // on a successive image, cropped appropriately
            for (String groupName : groupNameSet) {
                groupContext.getMessageReporter().logFormatted("Processing group '%s'", groupName);

                processRows(mapGroup.get(groupName), groupName, writer, groupContext);
            }
        } catch (InitializeException | ProvisionFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    private void processRows(
            Collection<CSVRow> rows,
            String groupName,
            ElementWriterSupplier<Collection<CSVRow>> writer,
            InputOutputContext groupContext) {
        if (rows != null && !rows.isEmpty()) {

            groupContext.getOutputter().writerPermissive().write(groupName, writer, () -> rows);

        } else {
            groupContext
                    .getMessageReporter()
                    .logFormatted("No matching rows for group '%s'", groupName);
        }
    }

    private DisplayStack createBackgroundStack(ImageInitialization params, Logger logger)
            throws OutputWriteFailedException {
        // Get our background-stack and objects. We duplicate to avoid threading issues
        StackProvider providerCopy = stack.duplicateBean();
        try {
            providerCopy.initializeRecursive(params, logger);
            return DisplayStack.create(providerCopy.get());
        } catch (CreateException | InitializeException | ProvisionFailedException e) {
            throw new OutputWriteFailedException(e);
        }
    }
}
