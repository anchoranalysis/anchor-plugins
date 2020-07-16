/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.comparison;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.csv.CSVWriter;

class CSVAssignment {

    private boolean includeDescriptiveSplit;
    private int maxSplitGroups;

    private Optional<CSVWriter> writer;

    private boolean firstRow = true;

    public CSVAssignment(
            BoundOutputManagerRouteErrors outputManager,
            String outputName,
            boolean includeDescriptiveSplit,
            int maxSplitGroups)
            throws AnchorIOException {
        super();
        this.includeDescriptiveSplit = includeDescriptiveSplit;
        this.maxSplitGroups = maxSplitGroups;

        writer = CSVWriter.createFromOutputManager(outputName, outputManager.getDelegate());
    }

    public synchronized void writeStatisticsForImage(
            Assignment assignment, SplitString descriptiveSplit, InputFromManager inputObject)
            throws OperationFailedException {

        if (!writer.isPresent() || !writer.get().isOutputEnabled()) {
            return;
        }

        if (firstRow) {
            firstRow = false;

            writer.get().writeHeaders(createHeaders(assignment));
        }

        writer.get().writeRow(createValues(assignment, inputObject, descriptiveSplit)); // NOSONAR
    }

    private List<String> createHeaders(Assignment assignment) {
        List<String> base = createBaseHeaders();
        base.addAll(assignment.createStatisticsHeaderNames());
        return base;
    }

    private List<String> createBaseHeaders() {
        List<String> headerNames = new ArrayList<>();
        headerNames.add("descriptiveName");
        headerNames.add("filePath");

        if (includeDescriptiveSplit) {
            for (int i = 0; i < maxSplitGroups; i++) {
                headerNames.add(String.format("dscr%d", i));
            }
        }

        return headerNames;
    }

    private List<TypedValue> createValues(
            Assignment assignment, InputFromManager inputObject, SplitString descriptiveSplit)
            throws OperationFailedException {
        List<TypedValue> base = createBaseValues(inputObject, descriptiveSplit);
        base.addAll(assignment.createStatistics());
        return base;
    }

    private List<TypedValue> createBaseValues(
            InputFromManager inputObject, SplitString descriptiveSplit)
            throws OperationFailedException {

        Elements rowElements = new Elements();

        try {
            rowElements.add(inputObject.descriptiveName());
            rowElements.add(inputObject.pathForBindingRequired().toString());
        } catch (AnchorIOException e) {
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
