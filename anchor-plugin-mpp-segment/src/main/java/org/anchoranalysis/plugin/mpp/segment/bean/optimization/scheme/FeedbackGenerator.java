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

package org.anchoranalysis.plugin.mpp.segment.bean.optimization.scheme;

import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.mpp.segment.bean.optimization.feedback.ExtractScoreSize;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackBeginParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackEndParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackReceiver;
import org.anchoranalysis.mpp.segment.optimization.feedback.ReporterException;
import org.anchoranalysis.mpp.segment.optimization.feedback.aggregate.AggregateTriggerBank;
import org.anchoranalysis.mpp.segment.optimization.feedback.aggregate.AggregatorException;
import org.anchoranalysis.mpp.segment.optimization.feedback.period.PeriodTriggerBank;
import org.anchoranalysis.mpp.segment.optimization.step.Reporting;

// NB we execute every feedback action in a separate thread
//  on the Swing's event dispatch thread
@RequiredArgsConstructor
class FeedbackGenerator<T> {

    // START REQUIRED BEANS
    private final FeedbackReceiver<T> feedbackReceiver;
    private final ErrorReporter errorReporter;
    // END REQUIRED BEANS

    private PeriodTriggerBank<T> periodTriggerBank;
    private AggregateTriggerBank<T> aggregateTriggerBank;

    public void begin(FeedbackBeginParameters<T> initialization, ExtractScoreSize<T> extracter) {

        this.periodTriggerBank = new PeriodTriggerBank<>();
        this.aggregateTriggerBank = new AggregateTriggerBank<>(extracter);

        initialization.setPeriodTriggerBank(periodTriggerBank);
        initialization.setAggregateTriggerBank(aggregateTriggerBank);

        try {
            feedbackReceiver.reportBegin(initialization);

            aggregateTriggerBank.start(initialization);
        } catch (AggregatorException | ReporterException e) {
            errorReporter.recordError(FeedbackGenerator.class, e);
        }
        periodTriggerBank.reset();
    }

    public void recordBest(Reporting<T> reporting) {

        try {
            feedbackReceiver.reportNewBest(reporting);
        } catch (ReporterException e) {
            errorReporter.recordError(FeedbackGenerator.class, e);
        }
    }

    public void trigger(Reporting<T> reporting) throws ReporterException {

        aggregateTriggerBank.trigger(reporting);

        try {
            periodTriggerBank.incr(reporting);
        } catch (OperationFailedException e) {
            errorReporter.recordError(FeedbackGenerator.class, e);
        }

        try {
            feedbackReceiver.reportItr(reporting);
        } catch (ReporterException e) {
            errorReporter.recordError(FeedbackGenerator.class, e);
        }
    }

    public void end(FeedbackEndParameters<T> step) {

        try {
            aggregateTriggerBank.end();
        } catch (AggregatorException e) {
            errorReporter.recordError(FeedbackGenerator.class, e);
        }
        try {
            feedbackReceiver.reportEnd(step);
        } catch (ReporterException e) {
            errorReporter.recordError(FeedbackGenerator.class, e);
        }
    }
}
