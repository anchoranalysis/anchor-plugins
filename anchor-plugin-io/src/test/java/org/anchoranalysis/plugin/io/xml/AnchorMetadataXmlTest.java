/* (C)2020 */
package org.anchoranalysis.plugin.io.xml;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.ParserConfigurationException;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.test.TestLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class AnchorMetadataXmlTest {

    @Rule public TemporaryFolder folder = new TemporaryFolder();

    private static ImageResolution createMockRes01() {
        return new ImageResolution(1.0, 2.0, 3.0);
    }

    @Test
    public void test()
            throws RasterIOException, ParserConfigurationException, SAXException, IOException,
                    URISyntaxException {

        ImageResolution sr = createMockRes01();

        Path pathOut = folder.newFile("mockRes01.xml").toPath();

        AnchorMetadataXml.writeResolutionXml(pathOut, sr);

        // basic test that the file exists, and nothing more
        assertTrue(Files.exists(pathOut));

        // check that the written xml file is the same as the version we already saved
        String pathSerializedMock01 = "mockRes01AsXml.xml";

        Document docFromFileSystem = TestLoader.openXmlAbsoluteFilePath(pathOut);

        TestLoader loader = TestLoader.createFromMavenWorkingDirectory();
        Document docGroundTruth = loader.openXmlFromTestPath(pathSerializedMock01);
        assertTrue(TestLoader.areXmlEqual(docFromFileSystem, docGroundTruth));
    }
}
