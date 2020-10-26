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

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.name.SimpleNameValue;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.core.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.bean.object.draw.Outline;
import org.anchoranalysis.image.io.stack.output.generator.RasterGeneratorDelegateToRaster;
import org.anchoranalysis.image.voxel.object.ObjectCollectionRTree;
import org.anchoranalysis.io.generator.collection.CollectionGenerator;
import org.anchoranalysis.io.generator.combined.CombinedListGenerator;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.io.input.path.DerivePathException;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.mpp.io.input.MPPInitParamsFactory;
import org.anchoranalysis.overlay.bean.DrawObject;
import org.anchoranalysis.plugin.mpp.experiment.bean.objects.columndefinition.ColumnDefinition;
import org.anchoranalysis.plugin.mpp.experiment.objects.FromCSVInput;
import org.anchoranalysis.plugin.mpp.experiment.objects.FromCSVSharedState;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.CSVRow;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.IndexedCSVRows;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.MapGroupToRow;

/**
 * Given a CSV with a point representing an object-mask, the object-masks are extracted from a
 * segmentation
 *
 * <p>The CSV filePath should be the same for all rasters passed as input. It gets cached between
 * the threads.
 *
 * @author Owen Feehan
 */
public class ExportObjectsFromCSV extends ExportObjectsBase<FromCSVInput, FromCSVSharedState> {

    private class CSVRowRGBOutlineGenerator
            extends RasterGeneratorDelegateToRaster<ObjectCollectionWithProperties, CSVRow> {

        /** All objects associated with the image. */
        private ObjectCollectionRTree allObjects;

        public CSVRowRGBOutlineGenerator(
                DrawObject drawObject,
                ObjectCollectionRTree allObjects,
                DisplayStack background,
                RGBColor colorFirst,
                RGBColor colorSecond) {
            super(
                    createRGBMaskGenerator(
                            drawObject, background, new ColorList(colorFirst, colorSecond)));
            this.allObjects = allObjects;
        }

        @Override
        protected ObjectCollectionWithProperties convertBeforeAssign(CSVRow element)
                throws OperationFailedException {
            return element.findObjectsMatchingRow(allObjects);
        }

        @Override
        protected Stack convertBeforeTransform(Stack stack) {
            return stack;
        }
    }

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter
    private DerivePath idGenerator; // Translates an input file name to a unique ID

    /** The stack that is the background for our objects */
    @BeanField @Getter @Setter private StackProvider stack;

    /** Column definition for the CSVfiles */
    @BeanField @Getter @Setter private ColumnDefinition columnDefinition;
    // END BEAN PROPERTIES

    private static final RGBColor COLOR_FIRST = new RGBColor(255, 0, 0);
    private static final RGBColor COLOR_SECOND = new RGBColor(0, 255, 0);

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(FromCSVInput.class);
    }

    @Override
    public FromCSVSharedState beforeAnyJobIsExecuted(
            Outputter outputter, ConcurrencyPlan concurrencyPlan, ParametersExperiment params)
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

        FromCSVInput input = inputBound.getInput();
        InputOutputContext context = inputBound.context();

        try {
            IndexedCSVRows groupedRows =
                    inputBound
                            .getSharedState()
                            .getIndexedRowsOrCreate(input.getCsvFilePath(), columnDefinition);

            // We look for rows that match our File ID
            String fileID =
                    idStringForPath(input.pathForBinding(), context.isDebugEnabled()).toString();
            MapGroupToRow mapGroup = groupedRows.get(fileID);

            if (mapGroup == null) {
                context.getMessageReporter()
                        .logFormatted(
                                "No matching rows in CSV file for id '%s' from '%s'",
                                fileID, input.pathForBinding());
                return;
            }

            processFileWithMap(
                    MPPInitParamsFactory.create(context, Optional.empty(), Optional.of(input))
                            .getImage(),
                    mapGroup,
                    groupedRows.groupNameSet(),
                    context);

        } catch (GetOperationFailedException
                | OperationFailedException
                | DerivePathException
                | CreateException e) {
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
        assert false;
        return super.defaultOutputs();
    }

    private DisplayStack createBackgroundStack(ImageInitParams so, Logger logger)
            throws CreateException, InitException {
        // Get our background-stack and objects. We duplicate to avoid threading issues
        StackProvider providerCopy = stack.duplicateBean();
        providerCopy.initRecursive(so, logger);
        return DisplayStack.create(providerCopy.create());
    }

    private Path idStringForPath(Optional<Path> path, boolean debugMode)
            throws DerivePathException {
        if (!path.isPresent()) {
            throw new DerivePathException(
                    "A binding-path is not present for the input, but is required");
        }

        return idGenerator != null ? idGenerator.deriveFrom(path.get(), debugMode) : path.get();
    }

    private void processFileWithMap(
            ImageInitParams imageInit,
            MapGroupToRow mapGroup,
            Set<String> groupNameSet,
            InputOutputContext context)
            throws OperationFailedException {

        try {
            DisplayStack background = createBackgroundStack(imageInit, context.getLogger());

            ObjectCollectionRTree objects =
                    new ObjectCollectionRTree(inputs(imageInit, context.getLogger()));

            // Loop through each group, and output a series of TIFFs, where set of objects is shown
            // on a successive image, cropped appropriately
            for (String groupName : groupNameSet) {
                context.getMessageReporter().logFormatted("Processing group '%s'", groupName);

                processRows(mapGroup.get(groupName), groupName, objects, background, context);
            }
        } catch (CreateException | InitException e) {
            throw new OperationFailedException(e);
        }
    }

    private void processRows(
            Collection<CSVRow> rows,
            String groupName,
            ObjectCollectionRTree objects,
            DisplayStack background,
            InputOutputContext context) {
        if (rows != null && !rows.isEmpty()) {
            outputGroup(
                    String.format("group_%s", groupName),
                    rows,
                    objects,
                    background,
                    context.getOutputter());
        } else {
            context.getMessageReporter().logFormatted("No matching rows for group '%s'", groupName);
        }
    }

    private void outputGroup(
            String label,
            Collection<CSVRow> rows,
            ObjectCollectionRTree objects,
            DisplayStack background,
            Outputter outputter) {

        outputter
                .writerPermissive()
                .write(
                        label,
                        () -> {
                            try {
                                return createGenerator(label, objects, background);
                            } catch (SetOperationFailedException e) {
                                throw new OutputWriteFailedException(e);
                            }
                        },
                        () -> rows);
    }

    private CollectionGenerator<CSVRow> createGenerator(
            String label, ObjectCollectionRTree objects, DisplayStack background)
            throws SetOperationFailedException {
        Outline outlineWriter = new Outline(1, false);

        CombinedListGenerator<CSVRow> listGenerator =
                new CombinedListGenerator<>(
                        Stream.of(
                                new SimpleNameValue<>(
                                        label,
                                        new CSVRowRGBOutlineGenerator(
                                                outlineWriter,
                                                objects,
                                                background,
                                                COLOR_FIRST,
                                                COLOR_SECOND)),
                                new SimpleNameValue<>("idXML", new CSVRowXMLGenerator())));

        // Output the group
        return new CollectionGenerator<>(listGenerator, "pair");
    }
}
