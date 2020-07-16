/* (C)2020 */
package org.anchoranalysis.io.manifest.reportfeature;

import java.io.IOException;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporterIntoLog;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.log.ConsoleMessageLogger;
import org.anchoranalysis.io.manifest.ManifestRecorder;
import org.anchoranalysis.io.manifest.ManifestRecorderFile;
import org.anchoranalysis.io.manifest.finder.FinderSerializedObject;

public class CfgSizeFromManifest extends ReportFeatureForManifest {

    @Override
    public String genFeatureStringFor(ManifestRecorderFile obj, Logger logger)
            throws OperationFailedException {

        FinderSerializedObject<Cfg> finder =
                new FinderSerializedObject<>(
                        "cfg", new ErrorReporterIntoLog(new ConsoleMessageLogger()));

        ManifestRecorder manifest = obj.doOperation();

        if (!finder.doFind(manifest)) {
            throw new OperationFailedException("Cannot find cfg in manifest");
        }

        try {
            return Integer.toString(finder.get().size());
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public String genTitleStr() throws OperationFailedException {
        return "cfgSize";
    }
}
