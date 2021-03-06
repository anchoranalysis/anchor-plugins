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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.value.TypedValue;
import org.anchoranalysis.io.generator.tabular.CSVWriter;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.Outputter;

class CSVAssignment {

    private boolean includeDescriptiveSplit;
    private int maxSplitGroups;

    private Optional<CSVWriter> writer;

    private boolean firstRow = true;

    public CSVAssignment(
            Outputter outputter,
            String outputName,
            boolean includeDescriptiveSplit,
            int maxSplitGroups)
            throws OutputWriteFailedException {
        super();
        this.includeDescriptiveSplit = includeDescriptiveSplit;
        this.maxSplitGroups = maxSplitGroups;

        writer = CSVWriter.createFromOutputter(outputName, outputter.getChecked());
    }

    public synchronized void writeStatisticsForImage(
            Assignment assignment, SplitString descriptiveSplit, InputFromManager input)
            throws OperationFailedException {

        if (!writer.isPresent() || !writer.get().isOutputEnabled()) {
            return;
        }

        if (firstRow) {
            firstRow = false;

            writer.get().writeHeaders(createHeaders(assignment));
        }

        writer.get().writeRow(createValues(assignment, input, descriptiveSplit)); // NOSONAR
    }

    private List<String> createHeaders(Assignment assignment) {
        List<String> base = createBaseHeaders();
        base.addAll(assignment.createStatisticsHeaderNames());
        return base;
    }

    private List<String> createBaseHeaders() {
        List<String> headerNames = new ArrayList<>();
        headerNames.add("inputName");
        headerNames.add("filePath");

        if (includeDescriptiveSplit) {
            for (int i = 0; i < maxSplitGroups; i++) {
                headerNames.add(String.format("dscr%d", i));
            }
        }

        return headerNames;
    }

    private List<TypedValue> createValues(
            Assignment assignment, InputFromManager input, SplitString descriptiveSplit)
            throws OperationFailedException {
        List<TypedValue> base = createBaseValues(input, descriptiveSplit);
        base.addAll(assignment.createStatistics());
        return base;
    }

    private List<TypedValue> createBaseValues(InputFromManager input, SplitString descriptiveSplit)
            throws OperationFailedException {

        Elements rowElements = new Elements();

        try {
            rowElements.add(input.name());
            rowElements.add(input.pathForBindingRequired().toString());
        } catch (InputReadFailedException e) {
            throw new OperationFailedException(e);
        }

        if (includeDescriptiveSplit) {
            for (int i = 0; i < maxSplitGroups; i++) {
                rowElements.add(descriptiveSplit.getSplitPartOrEmptyString(i));
            }
        }

        return rowElements.getList();
    }

    public void end() {
        writer.ifPresent(CSVWriter::close);
    }

    private static class Elements {
        private List<TypedValue> delegate = new ArrayList<>();

        public void add(String text) {
            delegate.add(new TypedValue(text, false));
        }

        public List<TypedValue> getList() {
            return delegate;
        }
    }
}
