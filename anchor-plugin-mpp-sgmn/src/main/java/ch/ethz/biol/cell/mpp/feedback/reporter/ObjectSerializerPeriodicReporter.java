/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import java.io.Serializable;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.generator.serialized.BundledObjectOutputStreamGenerator;
import org.anchoranalysis.io.manifest.deserializer.bundle.BundleParameters;
import org.anchoranalysis.io.manifest.sequencetype.SequenceType;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.PeriodicSubfolderReporter;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public abstract class ObjectSerializerPeriodicReporter<T extends Serializable>
        extends PeriodicSubfolderReporter<T> {

    // BEAN PARAMETERS
    @BeanField @Getter @Setter private String manifestFunction;

    @BeanField @Getter @Setter private int bundleSize = 1000;
    // END BEAN PARAMETERS

    public ObjectSerializerPeriodicReporter(String defaultManifestFunction) {
        super();
        this.manifestFunction = defaultManifestFunction;
    }

    @Override
    public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams)
            throws ReporterException {
        BundleParameters bundleParams = new BundleParameters();
        bundleParams.setBundleSize(bundleSize);

        try {
            super.reportBegin(initParams);

            SequenceType sequenceType =
                    init(
                            new BundledObjectOutputStreamGenerator<T>(
                                    bundleParams,
                                    this.generateOutputNameStyle(),
                                    getParentOutputManager().getDelegate(),
                                    manifestFunction));

            bundleParams.setSequenceType(sequenceType);

        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    protected abstract Optional<T> generateIterableElement(Reporting<CfgNRGPixelized> reporting)
            throws ReporterException;
}
