/* (C)2020 */
package org.anchoranalysis.test.mpp;

import java.nio.file.Path;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.mpp.io.cfg.CfgDeserializer;
import org.anchoranalysis.test.TestDataLoadException;
import org.anchoranalysis.test.TestLoader;

public class TestLoaderMPP {

    private TestLoader delegate;

    public TestLoaderMPP(TestLoader testLoader) {
        this.delegate = testLoader;
    }

    public Cfg openCfgFromTestPath(String testPath) {
        Path filePath = delegate.resolveTestPath(testPath);
        return openCfgFromFilePath(filePath);
    }

    public static Cfg openCfgFromFilePath(Path filePath) {

        CfgDeserializer deserializer = new CfgDeserializer();
        try {
            return deserializer.deserialize(filePath);
        } catch (DeserializationFailedException e) {
            throw new TestDataLoadException(e);
        }
    }
}
