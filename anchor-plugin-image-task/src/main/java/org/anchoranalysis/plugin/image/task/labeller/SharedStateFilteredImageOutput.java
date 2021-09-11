/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.labeller;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.value.TypedValue;
import org.anchoranalysis.feature.io.csv.FeatureCSVMetadata;
import org.anchoranalysis.feature.io.csv.FeatureCSVWriter;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.manifest.ManifestDirectoryDescription;
import org.anchoranalysis.io.manifest.sequencetype.StringsWithoutOrder;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.image.task.bean.labeller.ImageLabeller;

/**
 * @author Owen Feehan
 * @param <T> type of init-params for the ImageFilter
 */
public class SharedStateFilteredImageOutput<T> {

    private static final ManifestDirectoryDescription MANIFEST_DIRECTORY_LABELS =
            new ManifestDirectoryDescription(
                    "labels", "labelled_outputs", new StringsWithoutOrder());

    private ImageLabeller<T> filter;
    private Outputter baseOutputter;

    private Optional<GroupedMultiplexOutputters> outputters;

    private Optional<FeatureCSVWriter> csvWriter;

    private T initialization;

    private boolean groupIdentifierForCalled = false;

    private final String outputNameImages;

    /**
     * @param baseOutputter
     * @param filter the filter must not yet have been inited()
     * @param outputNameMapping the output-name for the CSV that is created showing the mapping
     *     between inputs and labels.
     * @param outputNameImages the output-name for the labels sub-directory in which copies images
     *     are placed in sub-directories.
     * @throws CreateException
     */
    public SharedStateFilteredImageOutput(
            Outputter baseOutputter,
            ImageLabeller<T> filter,
            String outputNameMapping,
            String outputNameImages)
            throws CreateException {

        this.baseOutputter = baseOutputter;
        this.filter = filter;
        this.outputNameImages = outputNameImages;

        // The CSV file with all names and corresponding groups
        try {
            this.csvWriter =
                    FeatureCSVWriter.create(
                            new FeatureCSVMetadata(
                                    outputNameMapping, Arrays.asList("name", "group")),
                            baseOutputter);
        } catch (OutputWriteFailedException e) {
            throw new CreateException(e);
        }
    }

    public synchronized void writeRow(String name, String groupIdentifier) {
        List<TypedValue> row = new ArrayList<>();
        row.add(new TypedValue(name));
        row.add(new TypedValue(groupIdentifier));

        csvWriter.ifPresent(writer -> writer.addRow(row));
    }

    public synchronized void close() {
        csvWriter.ifPresent(FeatureCSVWriter::close);
    }

    /** Determines a particular group-identifier for an input */
    public String labelFor(ProvidesStackInput input, InputOutputContext context)
            throws OperationFailedException {

        if (!groupIdentifierForCalled) {
            // We perform the necessary initialization of the filter, the first time
            //  this function is called. We do this so we have a sensible input-path
            //  to give to the filter.
            try {
                initFilterOutputters(input.pathForBindingRequired());
            } catch (InitializeException | InputReadFailedException e) {
                throw new OperationFailedException(e);
            }
            groupIdentifierForCalled = true;
        }

        return filter.labelFor(initialization, input, context);
    }

    /** groupIdentifierFor should always called at least once before getOutputManagerFor */
    public Optional<Outputter> getOutputterFor(String groupIdentifier) {
        return outputters.map(outputter -> outputter.getOutputterFor(groupIdentifier));
    }

    public T getFilterInitialization(Path pathForBinding) throws InitializeException {
        if (initialization == null) {
            initialization = filter.initialize(pathForBinding);
        }
        return initialization;
    }

    private void initFilterOutputters(Path pathForBinding) throws InitializeException {
        Optional<Outputter> outputterLabelsSubdirectory =
                baseOutputter
                        .writerSelective()
                        .createSubdirectory(outputNameImages, MANIFEST_DIRECTORY_LABELS, false);
        this.outputters =
                OptionalUtilities.map(
                        outputterLabelsSubdirectory,
                        directory ->
                                new GroupedMultiplexOutputters(
                                        directory,
                                        filter.allLabels(getFilterInitialization(pathForBinding))));
    }
}
