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

import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.image.voxel.object.IntersectingObjects;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.CSVRow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Pairwise extends ColumnDefinition {

    private ObjectInCSVColumn columnFirst;
    private ObjectInCSVColumn columnSecond;

    @Override
    public void initHeaders(String[] headers) throws InitializeException {
        super.initHeaders(headers);
        columnFirst = new ObjectInCSVColumn(indexUniquePixel, true);
        columnSecond = new ObjectInCSVColumn(indexUniquePixel, false);
    }

    @Override
    public ObjectCollectionWithProperties findObjectsMatchingRow(
            CSVRow csvRow, IntersectingObjects<ObjectMask> allObjects)
            throws OperationFailedException {

        ObjectCollectionWithProperties objects = new ObjectCollectionWithProperties();

        ObjectMask object1 = columnFirst.findObjectFromCSVRow(allObjects, csvRow);
        ObjectMask object2 = columnSecond.findObjectFromCSVRow(allObjects, csvRow);

        if (object1.equals(object2)) {
            throw new OperationFailedException(
                    String.format(
                            "Objects are identical at point %s and %s",
                            columnFirst.describeObject(csvRow),
                            columnSecond.describeObject(csvRow)));
        }

        objects.add(object1);
        objects.add(object2);
        return objects;
    }

    @Override
    public void writeToXML(CSVRow csvRow, Element xmlElement, Document doc) {

        AddToXMLDocument xmlDocument = new AddToXMLDocument(xmlElement, doc);

        String[] line = csvRow.getLine();

        xmlDocument.addObjectMask("First", line, columnFirst);
        xmlDocument.addObjectMask("Second", line, columnSecond);
    }
}
