/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.anchoranalysis.bean.xml.XmlUtilities;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.io.generator.Generator;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.xml.XMLGenerator;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.bean.OutputWriteSettings;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.xml.XmlOutputter;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.CSVRow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class CSVRowXMLGenerator extends XMLGenerator implements IterableGenerator<CSVRow> {

    private CSVRow element;

    @Override
    public CSVRow getIterableElement() {
        return element;
    }

    @Override
    public void setIterableElement(CSVRow element) throws SetOperationFailedException {
        this.element = element;
    }

    @Override
    public Generator getGenerator() {
        return this;
    }

    @Override
    public void writeToFile(OutputWriteSettings outputWriteSettings, Path filePath)
            throws OutputWriteFailedException {

        try {
            DocumentBuilder db = XmlUtilities.createDocumentBuilder();
            Document doc = db.newDocument();

            // create the root element and add it to the document
            Element root = doc.createElement("identify");
            doc.appendChild(root);

            element.writeToXML(root, doc);

            XmlOutputter.writeXmlToFile(doc, filePath);

        } catch (TransformerException | ParserConfigurationException | IOException e) {
            throw new OutputWriteFailedException(e);
        }
    }

    @Override
    public Optional<ManifestDescription> createManifestDescription() {
        return Optional.of(new ManifestDescription("xml", "objectPairsClass"));
    }
}
