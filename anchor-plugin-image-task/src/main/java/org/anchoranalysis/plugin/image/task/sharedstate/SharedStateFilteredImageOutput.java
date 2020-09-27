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

package org.anchoranalysis.plugin.image.task.sharedstate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.feature.io.csv.FeatureCSVMetadata;
import org.anchoranalysis.feature.io.csv.FeatureCSVWriter;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.image.task.bean.labeller.ImageLabeller;

/**
 * @author Owen Feehan
 * @param <T> type of init-params for the ImageFilter
 */
public class SharedStateFilteredImageOutput<T> {

    private ImageLabeller<T> filter;
    private Outputter baseOutputter;

    private GroupedMultiplexOutputters outputters;

    private Optional<FeatureCSVWriter> csvWriter;

    private T filterInitParams;

    private boolean groupIdentifierForCalled = false;

    /**
     * @param baseOutputter
     * @param filter the filter must not yet have been inited()
     * @throws CreateException
     */
    public SharedStateFilteredImageOutput(
            Outputter baseOutputter, ImageLabeller<T> filter)
            throws CreateException {

        this.baseOutputter = baseOutputter;
        this.filter = filter;

        // The CSV file with all names and corresponding groups
        try {
            this.csvWriter =
                    FeatureCSVWriter.create(
                            new FeatureCSVMetadata("group", Arrays.asList("name", "group")),
                            baseOutputter);
        } catch (AnchorIOException e) {
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
            } catch (InitException | AnchorIOException e) {
                throw new OperationFailedException(e);
            }
            groupIdentifierForCalled = true;
        }

        return filter.labelFor(filterInitParams, input, context);
    }

    /** groupIdentifierFor should always called at least once before getOutputManagerFor */
    public Outputter getOutputterFor(String groupIdentifier) {
        assert (groupIdentifierForCalled);
        return outputters.getOutputterFor(groupIdentifier);
    }

    public T getFilterInitParams(Path pathForBinding) throws InitException {
        if (filterInitParams == null) {
            filterInitParams = filter.init(pathForBinding);
        }
        return filterInitParams;
    }

    private void initFilterOutputters(Path pathForBinding) throws InitException {

        this.outputters =
                new GroupedMultiplexOutputters(
                        baseOutputter, filter.allLabels(getFilterInitParams(pathForBinding)));
    }
}
