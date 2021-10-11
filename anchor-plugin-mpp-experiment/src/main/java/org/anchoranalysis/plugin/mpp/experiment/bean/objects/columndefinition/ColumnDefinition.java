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
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.image.voxel.object.IntersectingObjects;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.CSVRow;
import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class ColumnDefinition extends AnchorBean<ColumnDefinition> {

    // START BEAN PROPERTIES
    /** Name of CSV column with the file identifier */
    @BeanField @Getter @Setter private String columnID = "id";

    /** Name of CSV column used to group results (the same values, are outputted together) */
    @BeanField @Getter @Setter private String columnGroup;

    /** Name of CSV column with X coordinate of point for the first Object */
    @BeanField @Getter @Setter private String columnUniquePixel = "unique_pixel_in_object";
    // END BEAN PROPERTIES

    private int indexFileID;
    private int indexGroup;
    protected int indexUniquePixel;

    public void initHeaders(String[] headers) throws InitializeException {
        // We resolve each of our columnNames to an index
        indexFileID = findHeaderIndex(headers, columnID);
        indexGroup = findHeaderIndex(headers, columnGroup);
        indexUniquePixel = findHeaderIndex(headers, columnUniquePixel);
    }

    public CSVRow createRow(String[] line) {
        CSVRow row = new CSVRow(this);
        row.setGroup(line[indexGroup]);
        row.setId(line[indexFileID]);
        row.setLine(line);
        return row;
    }

    public abstract ObjectCollectionWithProperties findObjectsMatchingRow(
            CSVRow csvRow, IntersectingObjects<ObjectMask> allObjects)
            throws OperationFailedException;

    public abstract void writeToXML(CSVRow csvRow, Element xmlElement, Document doc);

    /**
     * Finds the index of a particular column from the headers of a CSV file
     *
     * @param headers the headers
     * @param columnName the column whose index to find
     * @return the index of the first column to be equal (case-sensitive) to {@code columnName}
     * @throws InitializeException if the column-name does not exist in the headers
     */
    private static int findHeaderIndex(String[] headers, String columnName)
            throws InitializeException {
        int index = ArrayUtils.indexOf(headers, columnName);
        if (index == ArrayUtils.INDEX_NOT_FOUND) {
            throw new InitializeException(
                    String.format("Cannot find column '%s' among CSV file headers", columnName));
        }
        return index;
    }
}
