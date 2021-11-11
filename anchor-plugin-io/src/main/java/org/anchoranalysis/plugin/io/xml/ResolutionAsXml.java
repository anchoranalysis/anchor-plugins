/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.xml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.ToDoubleFunction;
import javax.xml.parsers.ParserConfigurationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.serialize.XMLParser;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.io.output.xml.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Reads and writes a metadata XML file specifying the image-resolution.
 *
 * <p>If a particular dimension is missing from the XML file, 1 is supplied as a replacement value.
 *
 * <p>The XML file specifies the physical pixel sizes (in meters) of an individual voxel, as
 * follows:
 *
 * <p>{@code
 * <metadata><resolution><width>0.12</width><height>0.12</height><depth>0.12</depth></resolution></metadata>}
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResolutionAsXml {

    private static final String ELEMENT_NAME_ROOT = "metadata";
    private static final String ELEMENT_NAME_RESOLUTION = "resolution";

    private static final String ELEMENT_NAME_X = "width";
    private static final String ELEMENT_NAME_Y = "height";
    private static final String ELEMENT_NAME_Z = "depth";

    /**
     * Retrieves resolution from a XML file previously written by {@link
     * #writeResolutionXml(Resolution, Path)}.
     *
     * @param file the file to open
     * @return a resolution read from the file
     * @throws ImageIOException if file I/O or parsing errors occur.
     */
    public static Resolution readResolutionXml(File file) throws ImageIOException {

        try {
            Document document = XMLParser.parse(file);
            document.getDocumentElement().normalize();

            NodeList allResolutionElements = document.getElementsByTagName(ELEMENT_NAME_RESOLUTION);

            if (allResolutionElements.getLength() != 1) {
                throw new ImageIOException("There must be only one resolution tag.");
            }

            return resolutionFromNodes(allResolutionElements.item(0).getChildNodes());

        } catch (ParserConfigurationException | SAXException | IOException | CreateException e) {
            throw new ImageIOException(
                    "An error occurred parsing a file containing image-resolution", e);
        }
    }

    /**
     * Writes a XML metadata file describing the image-resolution.
     *
     * @param path path to write to
     * @param resolution the resolution to write
     * @throws ImageIOException if any file I/O or parser errors occur
     */
    public static void writeResolutionXml(Resolution resolution, Path path)
            throws ImageIOException {

        try {
            Document document = XMLParser.createNewDocument();

            // create the root element and add it to the document
            Element root = document.createElement(ELEMENT_NAME_ROOT);
            document.appendChild(root);

            Element elementResolution = document.createElement(ELEMENT_NAME_RESOLUTION);
            root.appendChild(elementResolution);

            appendDimension(document, ELEMENT_NAME_X, resolution, Resolution::x, elementResolution);
            appendDimension(document, ELEMENT_NAME_Y, resolution, Resolution::y, elementResolution);
            appendDimension(document, ELEMENT_NAME_Z, resolution, Resolution::z, elementResolution);

            XMLWriter.writeXmlToFile(document, path);

        } catch (ParserConfigurationException | IOException e) {
            throw new ImageIOException(e.toString());
        }
    }

    private static void appendDimension(
            Document document,
            String elementName,
            Resolution resolution,
            ToDoubleFunction<Resolution> extractDimension,
            Element elementToAppendTo) {
        Element element = document.createElement(elementName);
        elementToAppendTo.appendChild(element);
        element.appendChild(textNode(document, resolution, extractDimension));
    }

    private static Text textNode(
            Document document,
            Resolution resolution,
            ToDoubleFunction<Resolution> extractDimension) {
        return document.createTextNode(Double.toString(extractDimension.applyAsDouble(resolution)));
    }

    private static Resolution resolutionFromNodes(NodeList nodes) throws CreateException {

        // Initialize to defaults
        Resolution resolution = new Resolution();
        double x = resolution.x();
        double y = resolution.y();
        double z = resolution.z();

        for (int i = 0; i < nodes.getLength(); i++) {

            Node node = nodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {

                if (node.getNodeName().equals(ELEMENT_NAME_X)) {
                    x = doubleFromNode(node);
                } else if (node.getNodeName().equals(ELEMENT_NAME_Y)) {
                    y = doubleFromNode(node);
                } else if (node.getNodeName().equals(ELEMENT_NAME_Z)) {
                    z = doubleFromNode(node);
                }
            }
        }

        return new Resolution(x, y, z);
    }

    private static double doubleFromNode(Node node) {
        return Double.parseDouble(node.getTextContent());
    }
}
