/* (C)2020 */
package org.anchoranalysis.plugin.io.xml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.xml.XmlUtilities;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.io.xml.XmlOutputter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnchorMetadataXml {

    // Opens the meta data, setting resolutions on the dimensions
    public static ImageResolution readResolutionXml(File fileMeta) throws RasterIOException {

        try {
            DocumentBuilder db = XmlUtilities.createDocumentBuilder();
            Document doc = db.parse(fileMeta);
            doc.getDocumentElement().normalize();

            NodeList resLst = doc.getElementsByTagName("resolution");

            if (resLst.getLength() != 1) {
                throw new RasterIOException("There must only one resolution tag");
            }

            return resFromNodeList(resLst.item(0).getChildNodes());

        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RasterIOException(e);
        }
    }

    public static void writeResolutionXml(Path filePath, ImageResolution res)
            throws RasterIOException {

        try {
            DocumentBuilder db = XmlUtilities.createDocumentBuilder();
            Document doc = db.newDocument();

            // create the root element and add it to the document
            Element root = doc.createElement("metadata");
            doc.appendChild(root);

            Element elmnRes = doc.createElement("resolution");
            root.appendChild(elmnRes);

            Element xRes = doc.createElement("xres");
            Element yRes = doc.createElement("yres");
            Element zRes = doc.createElement("zres");

            elmnRes.appendChild(xRes);
            elmnRes.appendChild(yRes);
            elmnRes.appendChild(zRes);

            xRes.appendChild(doc.createTextNode(Double.toString(res.getX())));
            yRes.appendChild(doc.createTextNode(Double.toString(res.getY())));
            zRes.appendChild(doc.createTextNode(Double.toString(res.getZ())));

            XmlOutputter.writeXmlToFile(doc, filePath);

        } catch (TransformerException | ParserConfigurationException | IOException e) {
            throw new RasterIOException(e.toString());
        }
    }

    private static ImageResolution resFromNodeList(NodeList nodeList) {

        // Initialize to defaults
        ImageResolution res = new ImageResolution();
        double x = res.getX();
        double y = res.getY();
        double z = res.getZ();

        for (int i = 0; i < nodeList.getLength(); i++) {

            Node n = nodeList.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {

                if (n.getNodeName().equals("xres")) {
                    x = doubleFromNode(n);
                } else if (n.getNodeName().equals("yres")) {
                    y = doubleFromNode(n);
                } else if (n.getNodeName().equals("zres")) {
                    z = doubleFromNode(n);
                }
            }
        }

        return new ImageResolution(x, y, z);
    }

    private static double doubleFromNode(Node n) {
        return Double.parseDouble(n.getTextContent());
    }
}
