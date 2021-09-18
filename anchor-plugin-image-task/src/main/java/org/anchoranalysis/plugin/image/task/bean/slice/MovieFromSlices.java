/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean.slice;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.progress.Progress;
import org.anchoranalysis.core.progress.ProgressIgnore;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.RGBChannelNames;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.image.io.channel.input.series.NamedChannelsForSeries;
import org.anchoranalysis.image.io.stack.output.OutputSequenceStackFactory;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.generator.sequence.OutputSequenceIncrementing;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.image.task.bean.RasterTask;

public class MovieFromSlices extends RasterTask<OutputSequenceIncrementing<Stack>, NoSharedState> {

    private static final String OUTPUT_FRAME = "frames";

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int delaySizeAtEnd = 0;

    @BeanField @Getter @Setter private String filePrefix = "movie";

    @BeanField @OptionalBean @Getter @Setter private SizeXY size;

    @BeanField @Getter @Setter private int startIndex = 0; // this does nothing atm

    @BeanField @Getter @Setter private int repeat = 1;
    // END BEAN PROPERTIES

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public OutputSequenceIncrementing<Stack> beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<NamedChannelsInput> inputs,
            ParametersExperiment params)
            throws ExperimentExecutionException {
        try {
            return OutputSequenceStackFactory.NO_RESTRICTIONS.incrementingByOneCurrentDirectory(
                    OUTPUT_FRAME, filePrefix, 8, outputter.getChecked());
        } catch (OutputWriteFailedException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    protected NoSharedState createSharedStateJob(InputOutputContext context)
            throws JobExecutionException {
        return NoSharedState.INSTANCE;
    }

    @Override
    public void startSeries(
            OutputSequenceIncrementing<Stack> sharedStateTask,
            NoSharedState sharedStateJob,
            InputOutputContext context)
            throws JobExecutionException {
        // NOTHING TO DO
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs().addEnabledOutputFirst(OUTPUT_FRAME);
    }

    @Override
    public void doStack(
            InputBound<NamedChannelsInput, OutputSequenceIncrementing<Stack>> input,
            NoSharedState sharedStateJob,
            int seriesIndex,
            int numberSeries,
            InputOutputContext context)
            throws JobExecutionException {

        try {
            NamedChannelsForSeries namedChannels =
                    input.getInput().createChannelsForSeries(0, ProgressIgnore.get());

            Progress progress = ProgressIgnore.get();

            Channel red = namedChannels.getChannel(RGBChannelNames.RED, 0, progress);
            Channel green = namedChannels.getChannel(RGBChannelNames.GREEN, 0, progress);
            Channel blue = namedChannels.getChannel(RGBChannelNames.BLUE, 0, progress);

            if (!red.dimensions().equals(blue.dimensions())
                    || !blue.dimensions().equals(green.dimensions())) {
                throw new JobExecutionException("Scene dimensions do not match");
            }

            Stack sliceOut = null;

            ExtractProjectedStack extract =
                    new ExtractProjectedStack(Optional.ofNullable(size).map(SizeXY::asExtent));

            for (int z = 0; z < red.dimensions().z(); z++) {

                sliceOut = extract.extractAndProjectStack(red, green, blue, z);

                for (int i = 0; i < repeat; i++) {
                    input.getSharedState().add(sliceOut);
                }
            }

            // Just
            if (delaySizeAtEnd > 0 && sliceOut != null) {
                for (int i = 0; i < delaySizeAtEnd; i++) {
                    input.getSharedState().add(sliceOut);
                }
            }

        } catch (ImageIOException
                | IncorrectImageSizeException
                | GetOperationFailedException
                | OutputWriteFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void endSeries(
            OutputSequenceIncrementing<Stack> sharedStateTask,
            NoSharedState sharedStateJob,
            InputOutputContext context)
            throws JobExecutionException {
        // NOTHING TO DO
    }

    @Override
    public void afterAllJobsAreExecuted(
            OutputSequenceIncrementing<Stack> sharedState, InputOutputContext context)
            throws ExperimentExecutionException {
        // NOTHING TO DO
    }
}
