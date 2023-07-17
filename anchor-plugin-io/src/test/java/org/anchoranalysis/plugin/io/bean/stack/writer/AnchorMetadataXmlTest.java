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

package org.anchoranalysis.plugin.io.bean.stack.writer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.ParserConfigurationException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.plugin.io.xml.ResolutionAsXML;
import org.anchoranalysis.test.CompareXML;
import org.anchoranalysis.test.TestLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

class AnchorMetadataXmlTest {

    @TempDir Path directory;

    @Test
    void test()
            throws ImageIOException, ParserConfigurationException, SAXException, IOException,
                    URISyntaxException {

        Resolution resolution = createMockResolution();

        Path pathOut = directory.resolve("mockRes01.xml");

        ResolutionAsXML.writeResolutionXML(resolution, pathOut);

        // basic test that the file exists, and nothing more
        assertTrue(Files.exists(pathOut));

        // check that the written xml file is the same as the version we already saved
        String pathSerializedMock01 = "mockRes01AsXml.xml";

        Document docFromFileSystem = TestLoader.openXmlAbsoluteFilePath(pathOut);

        TestLoader loader = TestLoader.createFromMavenWorkingDirectory();
        Document docGroundTruth = loader.openXmlFromTestPath(pathSerializedMock01);
        assertTrue(CompareXML.areDocumentsEqual(docFromFileSystem, docGroundTruth));
    }

    private static Resolution createMockResolution() {
        try {
            return new Resolution(1.0, 2.0, 3.0);
        } catch (CreateException e) {
            throw new AnchorImpossibleSituationException();
        }
    }
}
