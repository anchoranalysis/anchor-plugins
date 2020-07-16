/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.file.FileOutput;
import org.anchoranalysis.io.output.file.FileOutputFromManager;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CSVReporterUtilities {

    public static Optional<FileOutput> createFileOutputFor(
            String outputName,
            OptimizationFeedbackInitParams<CfgNRGPixelized> initParams,
            String manifestDscrFunction)
            throws OutputWriteFailedException {
        return FileOutputFromManager.create(
                "csv",
                Optional.of(new ManifestDescription("csv", manifestDscrFunction)),
                initParams.getInitContext().getOutputManager().getDelegate(),
                outputName);
    }
}
