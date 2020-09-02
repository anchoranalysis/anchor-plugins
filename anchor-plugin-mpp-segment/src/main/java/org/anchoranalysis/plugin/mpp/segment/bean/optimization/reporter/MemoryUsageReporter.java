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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.core.memory.MemoryUtilities;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.segment.bean.optimization.feedback.ReporterAgg;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackBeginParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.aggregate.AggregateReceiver;
import org.anchoranalysis.mpp.segment.optimization.feedback.aggregate.Aggregator;
import org.anchoranalysis.mpp.segment.optimization.step.Reporting;

public class MemoryUsageReporter extends ReporterAgg<VoxelizedMarksWithEnergy> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean showBest = true;

    @BeanField @Getter @Setter private boolean showAgg = true;
    // END BEAN PROPERTIES

    private MessageLogger logger;

    @Override
    protected AggregateReceiver<VoxelizedMarksWithEnergy> getAggregateReceiver() {
        return new AggregateReceiver<VoxelizedMarksWithEnergy>() {

            @Override
            public void aggStart(
                    FeedbackBeginParameters<VoxelizedMarksWithEnergy> initParams,
                    Aggregator agg) {
                logger = initParams.getInitContext().getLogger().messageLogger();
                MemoryUtilities.logMemoryUsage("MemoryUsageReporter step=start", logger);
            }

            @Override
            public void aggReport(Reporting<VoxelizedMarksWithEnergy> reporting, Aggregator agg) {

                if (!showAgg) {
                    return;
                }

                MemoryUtilities.logMemoryUsage(
                        String.format("MemoryUsageReporter AGG step=%d", reporting.getIter()),
                        logger);
            }

            @Override
            public void aggEnd(Aggregator agg) {
                MemoryUtilities.logMemoryUsage("MemoryUsageReporter step=end", logger);
            }
        };
    }

    @Override
    public void reportNewBest(Reporting<VoxelizedMarksWithEnergy> reporting) {

        if (!showBest) {
            return;
        }

        MemoryUtilities.logMemoryUsage(
                String.format("MemoryUsageReporter BEST step=%d", reporting.getIter()), logger);
    }
}
