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

package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.anchoranalysis.bean.xml.XmlUtilities;
import org.anchoranalysis.io.generator.xml.XMLGenerator;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.bean.OutputWriteSettings;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.xml.XMLWriter;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.CSVRow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class CSVRowXMLGenerator extends XMLGenerator<CSVRow> {

    @Override
    public void writeToFile(CSVRow element, OutputWriteSettings outputWriteSettings, Path filePath)
            throws OutputWriteFailedException {
        try {
            XMLWriter.writeXmlToFile(csvRowAsXml(element), filePath);
        } catch (OutputWriteFailedException | IOException e) {
            throw new OutputWriteFailedException(e);
        }
    }

    @Override
    public Optional<ManifestDescription> createManifestDescription() {
        return Optional.of(new ManifestDescription("xml", "objectPairsClass"));
    }

    private static Document csvRowAsXml(CSVRow element) throws OutputWriteFailedException {
        try {
            DocumentBuilder db = XmlUtilities.createDocumentBuilder();
            Document document = db.newDocument();

            // create the root element and add it to the document
            Element root = document.createElement("identify");
            document.appendChild(root);

            element.writeToXML(root, document);

            return document;
        } catch (ParserConfigurationException e) {
            throw new OutputWriteFailedException(e);
        }
    }
}
