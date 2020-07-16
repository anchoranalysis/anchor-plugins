/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.comparison;

import java.util.Optional;
import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.csv.CSVWriter;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroup;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroupList;

class CSVComparisonGroup<T extends Assignment> {

    private AnnotationGroupList<T> annotationGroupList;

    public CSVComparisonGroup(AnnotationGroupList<T> annotationGroupList) {
        super();
        this.annotationGroupList = annotationGroupList;
    }

    private void writeGroupStatsForGroup(AnnotationGroup<T> annotationGroup, CSVWriter writer) {

        writer.writeRow(annotationGroup.createValues());
    }

    public void writeGroupStats(BoundOutputManagerRouteErrors outputManager)
            throws AnchorIOException {

        Optional<CSVWriter> writer =
                CSVWriter.createFromOutputManager("byGroup", outputManager.getDelegate());

        if (!writer.isPresent()) {
            return;
        }

        try {
            writer.get().writeHeaders(annotationGroupList.first().createHeaders());

            for (AnnotationGroup<T> group : annotationGroupList) {
                writeGroupStatsForGroup(group, writer.get());
            }

        } finally {
            writer.get().close();
        }
    }
}
