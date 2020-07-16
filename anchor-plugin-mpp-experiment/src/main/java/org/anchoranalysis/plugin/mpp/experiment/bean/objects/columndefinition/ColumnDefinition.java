/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.objects.columndefinition;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.index.ObjectCollectionRTree;
import org.anchoranalysis.image.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.CSVRow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class ColumnDefinition extends AnchorBean<ColumnDefinition> {

    // START BEAN PROPERTIES

    /** Name of CSV column with the file identifier */
    @BeanField @Getter @Setter private String columnID = "id";

    /** Name of CSV column used to group results (the same values, are outputted together) */
    @BeanField @Getter @Setter private String columnGroup;
    // END BEAN PROPERTIES

    private int indexFileID;
    private int indexGroup;

    public void initHeaders(String[] headers) throws InitException {
        // We resolve each of our columnNames to an index
        indexFileID = HeaderFinder.findHeaderIndex(headers, columnID);
        indexGroup = HeaderFinder.findHeaderIndex(headers, columnGroup);
    }

    public CSVRow createRow(String[] line) {
        CSVRow row = new CSVRow(this);
        row.setGroup(line[indexGroup]);
        row.setId(line[indexFileID]);
        row.setLine(line);
        return row;
    }

    public abstract ObjectCollectionWithProperties findObjectsMatchingRow(
            CSVRow csvRow, ObjectCollectionRTree allObjects) throws OperationFailedException;

    public abstract void writeToXML(CSVRow csvRow, Element xmlElement, Document doc);
}
