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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.name.value.SimpleNameValue;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.extent.rtree.ObjectCollectionRTree;
import org.anchoranalysis.image.io.generator.raster.RasterGenerator;
import org.anchoranalysis.image.io.generator.raster.object.rgb.DrawCroppedObjectsGenerator;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.bean.object.writer.Outline;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.generator.Generator;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.collection.SubfolderGenerator;
import org.anchoranalysis.io.generator.combined.IterableCombinedListGenerator;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.io.input.MPPInitParamsFactory;
import org.anchoranalysis.overlay.bean.DrawObject;
import org.anchoranalysis.plugin.mpp.experiment.bean.objects.columndefinition.ColumnDefinition;
import org.anchoranalysis.plugin.mpp.experiment.objects.FromCSVInputObject;
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
public class ExportObjectsFromCSVTask
        extends ExportObjectsBase<FromCSVInputObject, FromCSVSharedState> {

    private class CSVRowRGBOutlineGenerator extends RasterGenerator
            implements IterableGenerator<CSVRow> {

        private DrawCroppedObjectsGenerator delegate;
        private CSVRow element;

        /** All objects associated with the image. */
        private ObjectCollectionRTree allObjects;

        public CSVRowRGBOutlineGenerator(
                DrawObject drawObject,
                ObjectCollectionRTree allObjects,
                DisplayStack background,
                RGBColor colorFirst,
                RGBColor colorSecond) {
            this.allObjects = allObjects;

            delegate =
                    createRGBMaskGenerator(
                            drawObject, background, new ColorList(colorFirst, colorSecond));
        }

        @Override
        public boolean isRGB() {
            return delegate.isRGB();
        }

        @Override
        public Stack generate() throws OutputWriteFailedException {
            return delegate.generate();
        }

        @Override
        public Optional<ManifestDescription> createManifestDescription() {
            return delegate.createManifestDescription();
        }

        @Override
        public CSVRow getIterableElement() {
            return element;
        }

        @Override
        public void setIterableElement(CSVRow element) throws SetOperationFailedException {
            this.element = element;

            try {
                delegate.setIterableElement(element.findObjectsMatchingRow(allObjects));

            } catch (OperationFailedException e) {
                throw new SetOperationFailedException(e);
            }
        }

        @Override
        public void start() throws OutputWriteFailedException {
            delegate.start();
        }

        @Override
        public void end() throws OutputWriteFailedException {
            delegate.end();
        }

        @Override
        public Generator getGenerator() {
            return this;
        }
    }

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter
    private FilePathGenerator idGenerator; // Translates an input file name to a unique ID

    /** The stack that is the background for our objects */
    @BeanField @Getter @Setter private StackProvider stack;

    /** Column definition for the CSVfiles */
    @BeanField @Getter @Setter private ColumnDefinition columnDefinition;
    // END BEAN PROPERTIES

    private static final RGBColor COLOR_FIRST = new RGBColor(255, 0, 0);
    private static final RGBColor COLOR_SECOND = new RGBColor(0, 255, 0);

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(FromCSVInputObject.class);
    }

    @Override
    public FromCSVSharedState beforeAnyJobIsExecuted(
            BoundOutputManagerRouteErrors outputManager,
            ConcurrencyPlan concurrencyPlan,
            ParametersExperiment params)
            throws ExperimentExecutionException {
        return new FromCSVSharedState();
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public void doJobOnInputObject(InputBound<FromCSVInputObject, FromCSVSharedState> input)
            throws JobExecutionException {

        FromCSVInputObject inputObject = input.getInputObject();
        BoundIOContext context = input.context();

        try {
            IndexedCSVRows groupedRows =
                    input.getSharedState()
                            .getIndexedRowsOrCreate(inputObject.getCsvFilePath(), columnDefinition);

            // We look for rows that match our File ID
            String fileID =
                    idStringForPath(inputObject.pathForBinding(), context.isDebugEnabled())
                            .toString();
            MapGroupToRow mapGroup = groupedRows.get(fileID);

            if (mapGroup == null) {
                context.getLogReporter()
                        .logFormatted(
                                "No matching rows in CSV file for id '%s' from '%s'",
                                fileID, inputObject.pathForBinding());
                return;
            }

            processFileWithMap(
                    MPPInitParamsFactory.create(context, Optional.empty(), Optional.of(inputObject))
                            .getImage(),
                    mapGroup,
                    groupedRows.groupNameSet(),
                    context);

        } catch (GetOperationFailedException
                | OperationFailedException
                | AnchorIOException
                | CreateException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(FromCSVSharedState sharedState, BoundIOContext context)
            throws ExperimentExecutionException {
        // NOTHING TO DO
    }

    private DisplayStack createBackgroundStack(ImageInitParams so, Logger logger)
            throws CreateException, InitException {
        // Get our background-stack and objects. We duplicate to avoid threading issues
        StackProvider providerCopy = stack.duplicateBean();
        providerCopy.initRecursive(so, logger);
        return DisplayStack.create(providerCopy.create());
    }

    private Path idStringForPath(Optional<Path> path, boolean debugMode) throws AnchorIOException {
        if (!path.isPresent()) {
            throw new AnchorIOException(
                    "A binding-path is not present for the input, but is required");
        }

        return idGenerator != null ? idGenerator.outFilePath(path.get(), debugMode) : path.get();
    }

    private void processFileWithMap(
            ImageInitParams imageInit,
            MapGroupToRow mapGroup,
            Set<String> groupNameSet,
            BoundIOContext context)
            throws OperationFailedException {

        try {
            DisplayStack background = createBackgroundStack(imageInit, context.getLogger());

            ObjectCollectionRTree objects =
                    new ObjectCollectionRTree(inputObjects(imageInit, context.getLogger()));

            // Loop through each group, and output a series of TIFFs, where set of objects is shown
            // on a successive image, cropped appropriately
            for (String groupName : groupNameSet) {
                context.getLogReporter().logFormatted("Processing group '%s'", groupName);

                Collection<CSVRow> rows = mapGroup.get(groupName);

                if (rows == null || rows.isEmpty()) {
                    context.getLogReporter()
                            .logFormatted("No matching rows for group '%s'", groupName);
                    continue;
                }

                // outputManager.resolveFolder(groupName, new FolderWritePhysical())
                outputGroup(
                        String.format("group_%s", groupName),
                        rows,
                        objects,
                        background,
                        context.getOutputManager());
            }
        } catch (CreateException | InitException e) {
            throw new OperationFailedException(e);
        }
    }

    private void outputGroup(
            String label,
            Collection<CSVRow> rows,
            ObjectCollectionRTree objects,
            DisplayStack background,
            BoundOutputManagerRouteErrors outputManager) {

        outputManager
                .getWriterAlwaysAllowed()
                .write(
                        label,
                        () -> {
                            try {
                                return createGenerator(label, rows, objects, background);
                            } catch (SetOperationFailedException e) {
                                throw new OutputWriteFailedException(e);
                            }
                        });
    }

    private SubfolderGenerator<CSVRow, Collection<CSVRow>> createGenerator(
            String label,
            Collection<CSVRow> rows,
            ObjectCollectionRTree objects,
            DisplayStack background)
            throws SetOperationFailedException {
        Outline outlineWriter = new Outline(1, false);

        IterableCombinedListGenerator<CSVRow> listGenerator =
                new IterableCombinedListGenerator<>(
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
        SubfolderGenerator<CSVRow, Collection<CSVRow>> subFolderGenerator =
                new SubfolderGenerator<>(listGenerator, "pair");
        subFolderGenerator.setIterableElement(rows);
        return subFolderGenerator;
    }
}
