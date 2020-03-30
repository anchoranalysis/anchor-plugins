package org.anchoranalysis.plugin.mpp.experiment.bean.objs;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import java.io.IOException;
import java.nio.file.Path;

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
import org.anchoranalysis.plugin.mpp.experiment.objs.csv.CSVRow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class CSVRowXMLGenerator extends XMLGenerator implements IterableGenerator<CSVRow> {

	private CSVRow element;
	
	@Override
	public CSVRow getIterableElement() {
		return element;
	}

	@Override
	public void setIterableElement(CSVRow element)
			throws SetOperationFailedException {
		this.element = element;
	}

	@Override
	public void start() throws OutputWriteFailedException {
	}

	@Override
	public void end() throws OutputWriteFailedException {
	}

	@Override
	public Generator getGenerator() {
		return this;
	}
	
	@Override
	public void writeToFile(OutputWriteSettings outputWriteSettings,
			Path filePath) throws OutputWriteFailedException {
		
		try {
			DocumentBuilder db = XmlUtilities.createDocumentBuilder();
			Document doc = db.newDocument();
			
			//create the root element and add it to the document
	        Element root = doc.createElement("identify");
	        doc.appendChild(root);
	
	        element.writeToXML(root, doc);

	        		        
	        XmlOutputter.writeXmlToFile( doc, filePath );
	       	        
		} catch (TransformerException | ParserConfigurationException | IOException e) {
			throw new OutputWriteFailedException(e);
		}        
	}

	@Override
	public ManifestDescription createManifestDescription() {
		return new ManifestDescription("xml", "objectPairsClass");
	}
	
}