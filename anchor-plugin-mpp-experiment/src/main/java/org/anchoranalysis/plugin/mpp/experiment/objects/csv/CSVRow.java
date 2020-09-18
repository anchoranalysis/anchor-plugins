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

package org.anchoranalysis.plugin.mpp.experiment.objects.csv;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.rtree.ObjectCollectionRTree;
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
