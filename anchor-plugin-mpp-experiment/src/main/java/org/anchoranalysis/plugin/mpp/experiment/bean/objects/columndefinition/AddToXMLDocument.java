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

import lombok.AllArgsConstructor;
import org.anchoranalysis.spatial.point.Point3i;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@AllArgsConstructor
class AddToXMLDocument {

    private static final String ELEMENT_NAME_NUMBER_VOXELS = "numPixels";
    private static final String ELEMENT_NAME_POINT = "point";

    /** Top-level element in XML document to which this class adds new children */
    private final Element topLevelParent;

    /** The XML document that is added to */
    private final Document document;

    /**
     * Adds child-elements to the {@code topLevelParent} to describe a particular object-mask
     *
     * @param suffix appended to the name of each new element
     * @param array array of elements, some of whom describe an object-mask
     * @param indices indices of which elements describe the object-mask
     */
    public void addObjectMask(String suffix, String[] array, ObjectInCsvRowIndices indices) {
        addInteger(
                ELEMENT_NAME_NUMBER_VOXELS + suffix,
                ArrayExtracter.getAsInt(array, indices.getNumberVoxels()));
        addPoint(ELEMENT_NAME_POINT + suffix, ArrayExtracter.getAsPoint(array, indices.getPoint()));
    }

    /**
     * Adds a child-element to the {@code topLevelParent} representing a Point encoded as three
     * integer elements in an array
     */
    private void addPoint(String elementName, Point3i point) {
        Element createdElement = document.createElement(elementName);

        topLevelParent.appendChild(createdElement);

        appendIntegerNode(createdElement, "x", point.x());
        appendIntegerNode(createdElement, "y", point.y());
        appendIntegerNode(createdElement, "z", point.z());
    }

    /**
     * Adds a child-element to the {@code topLevelParent} representing a particular integer element
     * from an array
     *
     * @param elementName element-name
     * @param val value of element
     */
    private void addInteger(String elementName, int val) {
        appendIntegerNode(topLevelParent, elementName, val);
    }

    private void appendIntegerNode(Element parentElement, String elementName, int val) {
        Element element = document.createElement(elementName);
        parentElement.appendChild(element);
        element.appendChild(document.createTextNode(Integer.toString(val)));
    }
}
