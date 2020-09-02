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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.xml.XmlUtilities;
import org.anchoranalysis.image.extent.Resolution;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.io.xml.XmlOutputter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Reads and writes a metadata XML file specifying the image-resolution.
 * 
 * @author Owen Feehan
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnchorMetadataXml {
    
    private static final String ELEMENT_NAME_X = "xres";
    private static final String ELEMENT_NAME_Y = "yres";
    private static final String ELEMENT_NAME_Z = "zres";
    
    /**
     * Retrieves resolution from a XML file previously written by {@link #writeResolutionXml(Resolution, Path)}.
     * 
     * @param file the file to open
     * @return a resolution read from the file
     * @throws RasterIOException if file I/O or parsing errors occur.
     */
    public static Resolution readResolutionXml(File file) throws RasterIOException {

        try {
            DocumentBuilder builder = XmlUtilities.createDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();

            NodeList allResolutionElements = document.getElementsByTagName("resolution");

            if (allResolutionElements.getLength() != 1) {
                throw new RasterIOException("There must be only one resolution tag.");
            }

            return resolutionFromNodes(allResolutionElements.item(0).getChildNodes());

        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RasterIOException(e);
        }
    }

    /**
     * Writes a XML metadata file describing the image-resolution.
     * 
     * @param path path to write to
     * @param resolution the resolution to write
     * @throws RasterIOException if any file I/O or parser errors occur
     */
    public static void writeResolutionXml(Resolution resolution, Path path) throws RasterIOException {

        try {
            DocumentBuilder builder = XmlUtilities.createDocumentBuilder();
            Document document = builder.newDocument();

            // create the root element and add it to the document
            Element root = document.createElement("metadata");
            document.appendChild(root);

            Element elementResolution = document.createElement("resolution");
            root.appendChild(elementResolution);

            appendDimension(document, ELEMENT_NAME_X, resolution, Resolution::x, elementResolution);
            appendDimension(document, ELEMENT_NAME_Y, resolution, Resolution::y, elementResolution);
            appendDimension(document, ELEMENT_NAME_Z, resolution, Resolution::z, elementResolution);

            XmlOutputter.writeXmlToFile(document, path);

        } catch (TransformerException | ParserConfigurationException | IOException e) {
            throw new RasterIOException(e.toString());
        }
    }
    
    private static void appendDimension(Document document, String elementName, Resolution resolution, ToDoubleFunction<Resolution> extractDimension, Element elementToAppendTo) {
        Element element = document.createElement(elementName);
        elementToAppendTo.appendChild(element);
        element.appendChild( textNode(document, resolution, extractDimension) );
    }
    
    private static Text textNode(Document document, Resolution resolution, ToDoubleFunction<Resolution> extractDimension) {
        return document.createTextNode(Double.toString( extractDimension.applyAsDouble(resolution) ));
    }

    private static Resolution resolutionFromNodes(NodeList nodes) {

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
