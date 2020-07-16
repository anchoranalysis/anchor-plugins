/* (C)2020 */
package org.anchoranalysis.plugin.image.task.sharedstate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.feature.io.csv.writer.FeatureCSVWriter;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;

public class SharedStateSelectedSlice {

    private Optional<FeatureCSVWriter> csvWriter;

    public SharedStateSelectedSlice(BoundOutputManagerRouteErrors baseOutputManager)
            throws CreateException {
        super();

        try {
            this.csvWriter =
                    FeatureCSVWriter.create(
                            "selectedSlices",
                            baseOutputManager,
                            new String[] {"name"},
                            createFeatureNames());
        } catch (AnchorIOException e) {
            throw new CreateException(e);
        }
    }

    public synchronized void writeRow(String name, int selectedSliceIndex, double featureOptima) {
        List<TypedValue> row =
                Arrays.asList(
                        new TypedValue(name),
                        new TypedValue(selectedSliceIndex),
                        new TypedValue(featureOptima, 7));
        this.csvWriter.ifPresent(writer -> writer.addRow(row));
    }

    private static FeatureNameList createFeatureNames() {
        return new FeatureNameList(Stream.of("sliceIndex", "featureOptima"));
    }

    public void close() {
        csvWriter.ifPresent(FeatureCSVWriter::close);
    }
}
