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
import org.anchoranalysis.feature.io.csv.FeatureCSVWriter;
import org.anchoranalysis.feature.io.csv.metadata.FeatureCSVMetadata;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.image.task.bean.labeller.ImageLabeller;

/**
 * Manages filtered image output with shared state across multiple images.
 *
 * @param <T> type of initialization for the {@link ImageLabeller}.
 */
public class SharedStateFilteredImageOutput<T> {

    /** The image labeller used to filter and label images. */
    private ImageLabeller<T> filter;

    /** The base outputter for writing results. */
    private Outputter baseOutputter;

    /** Optional grouped multiplexed outputters for different labels. */
    private Optional<GroupedMultiplexOutputters> outputters;

    /** Optional CSV writer for recording image names and their corresponding groups. */
    private Optional<FeatureCSVWriter> csvWriter;

    /** The initialization data for the image labeller. */
    private T initialization;

    /** Flag to track if the groupIdentifierFor method has been called. */
    private boolean groupIdentifierForCalled = false;

    /** The output name for the labels sub-directory where image copies are placed. */
    private final String outputNameImages;

    /**
     * Creates a new {@link SharedStateFilteredImageOutput}.
     *
     * @param baseOutputter the base {@link Outputter} for writing results
     * @param filter the {@link ImageLabeller} to use (must not be initialized yet)
     * @param outputNameMapping the output name for the CSV file mapping inputs to labels
     * @param outputNameImages the output name for the labels sub-directory
     * @throws CreateException if there's an error during creation
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
                            baseOutputter,
                            false);
        } catch (OutputWriteFailedException e) {
            throw new CreateException(e);
        }
    }

    /**
     * Writes a row to the CSV file with image name and group identifier.
     *
     * @param name the name of the image
     * @param groupIdentifier the group identifier for the image
     */
    public synchronized void writeRow(String name, String groupIdentifier) {
        List<TypedValue> row = new ArrayList<>();
        row.add(new TypedValue(name));
        row.add(new TypedValue(groupIdentifier));

        csvWriter.ifPresent(writer -> writer.addRow(row));
    }

    /** Closes the CSV writer. */
    public synchronized void close() {
        csvWriter.ifPresent(FeatureCSVWriter::close);
    }

    /**
     * Determines a particular group-identifier for an input.
     *
     * @param input the {@link ProvidesStackInput} to label
     * @param context the {@link InputOutputContext} for the operation
     * @return the label for the input
     * @throws OperationFailedException if the labelling operation fails
     */
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

    /**
     * Gets the outputter for a specific group identifier.
     *
     * @param groupIdentifier the group identifier
     * @return an {@link Optional} containing the {@link Outputter} for the group, if it exists
     */
    public Optional<Outputter> getOutputterFor(String groupIdentifier) {
        return outputters.map(outputter -> outputter.getOutputterFor(groupIdentifier));
    }

    /**
     * Gets the filter initialization, initializing it if necessary.
     *
     * @param pathForBinding the {@link Path} to use for initialization
     * @return the initialization data
     * @throws InitializeException if initialization fails
     */
    public T getFilterInitialization(Path pathForBinding) throws InitializeException {
        if (initialization == null) {
            initialization = filter.initialize(pathForBinding);
        }
        return initialization;
    }

    /**
     * Initializes the filter outputters.
     *
     * @param pathForBinding the {@link Path} to use for initialization
     * @throws InitializeException if initialization fails
     */
    private void initFilterOutputters(Path pathForBinding) throws InitializeException {
        Optional<Outputter> outputterLabelsSubdirectory =
                baseOutputter.writerSelective().createSubdirectory(outputNameImages, false);
        this.outputters =
                OptionalUtilities.map(
                        outputterLabelsSubdirectory,
                        directory ->
                                new GroupedMultiplexOutputters(
                                        directory,
                                        filter.allLabels(getFilterInitialization(pathForBinding))));
    }
}
