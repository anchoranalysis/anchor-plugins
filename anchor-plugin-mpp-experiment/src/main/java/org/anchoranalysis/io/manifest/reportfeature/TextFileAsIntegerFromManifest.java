/* (C)2020 */
package org.anchoranalysis.io.manifest.reportfeature;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.io.manifest.ManifestRecorder;
import org.anchoranalysis.io.manifest.ManifestRecorderFile;
import org.anchoranalysis.io.manifest.finder.FinderFileAsText;

public class TextFileAsIntegerFromManifest extends ReportFeatureForManifestFileBase {

    @Override
    public String genFeatureStringFor(ManifestRecorderFile obj, Logger logger)
            throws OperationFailedException {

        FinderFileAsText finder = new FinderFileAsText(getFileName(), null);

        ManifestRecorder manifest = obj.doOperation();

        if (!finder.doFind(manifest)) {
            throw new OperationFailedException(
                    String.format("Cannot find '%s' in manifest", getFileName()));
        }

        try {
            return finder.get().trim().trim();
        } catch (GetOperationFailedException e) {
            throw new OperationFailedException(e);
        }
    }
}
