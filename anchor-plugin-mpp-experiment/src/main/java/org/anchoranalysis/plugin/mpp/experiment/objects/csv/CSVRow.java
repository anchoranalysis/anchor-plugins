/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.objects.csv;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.index.ObjectCollectionRTree;
import org.anchoranalysis.image.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.plugin.mpp.experiment.bean.objects.columndefinition.ColumnDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A row of the CSV File
 *
 * <p>We keep a pointer to the columnDefinition, so we can parse the CSV lines later as needed
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
public class CSVRow {

    // START REQUIRED ARGUMENTS
    private final ColumnDefinition columnDefinition;
    // END REQUIRED ARGUMENTS

    @Getter @Setter private String id;

    @Getter @Setter private String group;

    @Getter @Setter private String[] line;

    public ObjectCollectionWithProperties findObjectsMatchingRow(ObjectCollectionRTree allObjects)
            throws OperationFailedException {
        return columnDefinition.findObjectsMatchingRow(this, allObjects);
    }

    public void writeToXML(Element xmlElement, Document doc) {
        columnDefinition.writeToXML(this, xmlElement, doc);
    }
}
