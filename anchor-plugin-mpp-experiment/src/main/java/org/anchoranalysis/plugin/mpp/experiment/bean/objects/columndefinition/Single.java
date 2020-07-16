/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.objects.columndefinition;

import java.util.Arrays;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.index.ObjectCollectionRTree;
import org.anchoranalysis.image.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.CSVRow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Single extends ColumnDefinition {

    // START BEAN PROPERTIES
    /** Name of CSV column with X coordinate of point for the first Object */
    @BeanField @Getter @Setter private String columnPointX = "insidePoint.x";

    /** Name of CSV column with Y coordinate of point for the first Object */
    @BeanField @Getter @Setter private String columnPointY = "insidePoint.y";

    /** Name of CSV column with Z coordinate of point for the first Object */
    @BeanField @Getter @Setter private String columnPointZ = "insidePoint.z";

    /** Name of CSV column with the number of pixels for the first Object */
    @BeanField @Getter @Setter private String columnNumPixels = "numPixels";
    // END BEAN PROPERTIES

    private ObjectInCsvRowIndices indices;

    @Override
    public void initHeaders(String[] headers) throws InitException {
        super.initHeaders(headers);

        // We resolve each of our columnNames to an index
        indices =
                HeaderFinder.findHeadersToDescribeObject(
                        headers,
                        columnNumPixels,
                        Arrays.asList(columnPointX, columnPointY, columnPointZ));
    }

    @Override
    public ObjectCollectionWithProperties findObjectsMatchingRow(
            CSVRow csvRow, ObjectCollectionRTree allObjects) throws OperationFailedException {

        ObjectCollectionWithProperties objects = new ObjectCollectionWithProperties();
        objects.add(indices.findObjectFromCSVRow(allObjects, csvRow));
        return objects;
    }

    @Override
    public void writeToXML(CSVRow csvRow, Element xmlElement, Document doc) {

        AddToXMLDocument xmlDocument = new AddToXMLDocument(xmlElement, doc);

        xmlDocument.addObjectMask("", csvRow.getLine(), indices);
    }
}
