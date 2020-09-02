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

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.generator.serialized.XStreamGenerator;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.segment.bean.optimization.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackEndParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackBeginParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.ReporterException;
import org.anchoranalysis.mpp.segment.optimization.step.Reporting;
import org.apache.commons.lang.time.StopWatch;

public class MinimalExecutionTimeStatsReporter
        extends FeedbackReceiverBean<VoxelizedMarksWithEnergy> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String outputName = "minimalExecutionTimeStats";
    // END BEAN PROPERTIES

    private BoundOutputManagerRouteErrors outputManager = null;
    private KernelExecutionStats stats;
    private StopWatch stopWatch;

    @Override
    public void reportBegin(FeedbackBeginParameters<VoxelizedMarksWithEnergy> initParams)
            throws ReporterException {
        outputManager = initParams.getInitContext().getOutputManager();
        stats = new KernelExecutionStats(initParams.getKernelFactoryList().size());
        stopWatch = new StopWatch();
        stopWatch.start();
    }

    @Override
    public void reportItr(Reporting<VoxelizedMarksWithEnergy> reporting) {

        int kernelID = reporting.kernelIdentifier();
        double executionTime = reporting.getExecutionTime();

        if (reporting.getProposal().isPresent()) {

            if (reporting.isAccepted()) {
                stats.incrAccepted(kernelID, executionTime);
            } else {
                stats.incrRejected(kernelID, executionTime);
            }
        } else {
            // No proposal
            stats.incrNotProposed(kernelID, executionTime);
        }
    }

    @Override
    public void reportNewBest(Reporting<VoxelizedMarksWithEnergy> reporting) {
        // NOTHING TO DO
    }

    @Override
    public void reportEnd(FeedbackEndParameters<VoxelizedMarksWithEnergy> params) {
        stats.setTotalExecutionTime(stopWatch.getTime());
        stopWatch.stop();

        outputManager
                .getWriterCheckIfAllowed()
                .write(
                        outputName,
                        () ->
                                new XStreamGenerator<>(
                                        stats, Optional.of("minimalExecutionTimeStats")));
    }
}
