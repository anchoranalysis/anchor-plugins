/*-
 * #%L
 * anchor-plugin-mpp-sgmn
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
