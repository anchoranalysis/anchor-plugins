/*-
 * #%L
 * anchor-plugin-mpp-experiment
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
