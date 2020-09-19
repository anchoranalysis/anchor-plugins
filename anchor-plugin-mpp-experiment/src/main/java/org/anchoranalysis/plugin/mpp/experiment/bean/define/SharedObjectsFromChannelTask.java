/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.mpp.experiment.bean.define;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.experiment.identifiers.StackIdentifiers;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.generator.raster.ChannelGenerator;
import org.anchoranalysis.image.io.input.NamedChannelsInput;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeries;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.segment.bean.define.DefineOutputterMPP;

public class SharedObjectsFromChannelTask extends RasterTask {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private DefineOutputterMPP define;

    @BeanField @Getter @Setter private String outputNameOriginal = "original";
    // END BEAN PROPERTIES

    @Override
    public void doStack(
            NamedChannelsInput inputObject, int seriesIndex, int numSeries, BoundIOContext context)
            throws JobExecutionException {

        NamedChannelsForSeries ncc;
        try {
            ncc = inputObject.createChannelsForSeries(0, ProgressReporterNull.get());
        } catch (RasterIOException e1) {
            throw new JobExecutionException(e1);
        }

        try {
            Optional<Channel> inputImage =
                    ncc.getChannelOptional(
                            StackIdentifiers.INPUT_IMAGE, 0, ProgressReporterNull.get());
            inputImage.ifPresent(
                    image ->
                            context.getOutputManager()
                                    .getWriterCheckIfAllowed()
                                    .write(
                                            outputNameOriginal,
                                            () -> new ChannelGenerator("original",image)));

            define.processInput(ncc, context);

        } catch (GetOperationFailedException | OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void startSeries(
            BoundOutputManagerRouteErrors outputManager, ErrorReporter errorReporter)
            throws JobExecutionException {
        // NOTHING TO DO
    }

    @Override
    public void endSeries(BoundOutputManagerRouteErrors outputManager)
            throws JobExecutionException {
        // NOTHING TO DO
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }
}
