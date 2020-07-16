/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.objects.columndefinition;

import lombok.AllArgsConstructor;
import org.anchoranalysis.core.geometry.Point3i;
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

        appendIntegerNode(createdElement, "x", point.getX());
        appendIntegerNode(createdElement, "y", point.getY());
        appendIntegerNode(createdElement, "z", point.getZ());
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
