/* (C)2020 */
package org.anchoranalysis.io.manifest.reportfeature;

import java.io.IOException;
import java.nio.file.Path;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.io.manifest.ManifestRecorderFile;
import org.anchoranalysis.io.manifest.finder.FinderFileAsText;

public class TextFileAsIntegerFromPath extends ReportFeatureForManifestFileBase {

    @Override
    public String genFeatureStringFor(ManifestRecorderFile obj, Logger logger)
            throws OperationFailedException {

        Path executionTimePath = obj.getRootPath().resolve(getFileName() + ".txt");

        if (executionTimePath.toFile().exists()) {
            String execTime;
            try {
                execTime = FinderFileAsText.readFile(executionTimePath);
            } catch (IOException e) {
                throw new OperationFailedException(e);
            }
            return execTime.trim().trim();
        } else {
            throw new OperationFailedException(
                    String.format("Cannot find '%s'.txt in same folder as root", getFileName()));
        }
    }
}
