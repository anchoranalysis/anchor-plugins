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

package org.anchoranalysis.plugin.mpp.segment.bean.optimization.reporter;

import java.io.Serializable;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.generator.serialized.ObjectOutputStreamGenerator;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.segment.bean.optimization.feedback.PeriodicSubdirectoryReporter;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackBeginParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.ReporterException;
import org.anchoranalysis.mpp.segment.optimization.step.Reporting;

public abstract class ObjectSerializerPeriodicReporter<T extends Serializable>
        extends PeriodicSubdirectoryReporter<T> {

    // BEAN PARAMETERS
    @BeanField @Getter @Setter private String manifestFunction;

    @BeanField @Getter @Setter private int bundleSize = 1000;
    // END BEAN PARAMETERS

    protected ObjectSerializerPeriodicReporter(String defaultManifestFunction) {
        this.manifestFunction = defaultManifestFunction;
    }

    @Override
    public void reportBegin(FeedbackBeginParameters<VoxelizedMarksWithEnergy> initialization)
            throws ReporterException {

        try {
            super.reportBegin(initialization);
            initialize(new ObjectOutputStreamGenerator<>(Optional.of(manifestFunction)));

        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    protected abstract Optional<T> generateIterableElement(
            Reporting<VoxelizedMarksWithEnergy> reporting) throws ReporterException;
}
