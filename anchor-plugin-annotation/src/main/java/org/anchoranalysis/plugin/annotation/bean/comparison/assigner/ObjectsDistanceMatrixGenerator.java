/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.comparison.assigner;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.annotation.io.assignment.ObjectCollectionDistanceMatrix;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.generator.csv.CSVGenerator;
import org.anchoranalysis.io.output.bean.OutputWriteSettings;
import org.anchoranalysis.io.output.csv.CSVWriter;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

class ObjectsDistanceMatrixGenerator extends CSVGenerator {

    private static final String MANIFEST_FUNCTION = "objectCollectionDistanceMatrix";

    private final ObjectCollectionDistanceMatrix distanceMatrix;
    private final int numDecimalPlaces;

    public ObjectsDistanceMatrixGenerator(
            ObjectCollectionDistanceMatrix distanceMatrix, int numDecimalPlaces) {
        super(MANIFEST_FUNCTION);
        this.distanceMatrix = distanceMatrix;
        this.numDecimalPlaces = numDecimalPlaces;
    }

    @Override
    public void writeToFile(OutputWriteSettings outputWriteSettings, Path filePath)
            throws OutputWriteFailedException {

        try (CSVWriter csvWriter = CSVWriter.create(filePath)) {

            // A sensible header string, bearing in the mind the first column has object
            // descriptions
            List<String> headers = descriptionFromObjects(distanceMatrix.getObjects2());
            headers.add(0, "Objects");

            csvWriter.writeHeaders(headers);

            // The descriptions of objects1 go in the first column
            List<String> column0 = descriptionFromObjects(distanceMatrix.getObjects1());

            for (int i = 0; i < distanceMatrix.sizeObjects1(); i++) {
                List<TypedValue> row = rowFromDistanceMatrix(i);

                // Insert the description
                row.add(0, new TypedValue(column0.get(i)));

                csvWriter.writeRow(row);
            }
        } catch (AnchorIOException e) {
            throw new OutputWriteFailedException(e);
        }
    }

    private List<TypedValue> rowFromDistanceMatrix(int indx1) {
        List<TypedValue> out = new ArrayList<>();

        for (int indx2 = 0; indx2 < distanceMatrix.sizeObjects2(); indx2++) {
            out.add(new TypedValue(distanceMatrix.getDistance(indx1, indx2), numDecimalPlaces));
        }

        return out;
    }

    // A description of each object in a collection
    private static List<String> descriptionFromObjects(ObjectCollection objects) {
        return objects.stream().mapToList(objectMask -> objectMask.centerOfGravity().toString());
    }
}
