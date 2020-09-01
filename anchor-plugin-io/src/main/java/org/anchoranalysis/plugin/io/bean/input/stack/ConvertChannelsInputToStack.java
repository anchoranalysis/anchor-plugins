/*-
 * #%L
 * anchor-mpp-io
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
package org.anchoranalysis.plugin.io.bean.input.stack;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.name.store.NamedProviderStore;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterMultiple;
import org.anchoranalysis.core.progress.ProgressReporterOneOfMany;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.input.NamedChannelsInput;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeries;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.TimeSequence;
import org.anchoranalysis.plugin.io.bean.input.stack.StackSequenceInput;
import lombok.AllArgsConstructor;

/**
 * An input object that converts {@link NamedChannelsInput} to {@link StackSequenceInput}
 *
 * @author Owen Feehan
 */
@AllArgsConstructor public class ConvertChannelsInputToStack implements StackSequenceInput {

    /** Input to convert */
    private NamedChannelsInput input;

    /** Time-index to convert */
    private int timeIndex = 0;
        
    /** By default all channels are converted into a stack. If set, only this channel is converted into a stack. */
    private Optional<String> channelName;

    public ConvertChannelsInputToStack(NamedChannelsInput input) {
        this(input, 0, Optional.empty());
    }
    
    @Override
    public String descriptiveName() {
        return input.descriptiveName();
    }

    @Override
    public Optional<Path> pathForBinding() {
        return input.pathForBinding();
    }

    @Override
    public void close(ErrorReporter errorReporter) {
        input.close(errorReporter);
    }

    @Override
    public TimeSequenceSupplier createStackSequenceForSeries(int seriesNum)
            throws RasterIOException {
        return progressReporter -> convert(progressReporter, input, seriesNum);
    }

    @Override
    public void addToStoreInferNames(
            NamedProviderStore<TimeSequence> stackCollection,
            int seriesNum,
            ProgressReporter progressReporter)
            throws OperationFailedException {
        input.addToStoreInferNames(stackCollection, seriesNum, progressReporter);
    }

    @Override
    public void addToStoreWithName(
            String name,
            NamedProviderStore<TimeSequence> stackCollection,
            int seriesNum,
            ProgressReporter progressReporter)
            throws OperationFailedException {
        input.addToStoreWithName(name, stackCollection, seriesNum, progressReporter);
    }

    @Override
    public int numberFrames() throws OperationFailedException {
        return input.numberFrames();
    }

    private TimeSequence convert(
            ProgressReporter progressReporter, NamedChannelsInput in, int seriesNum)
            throws OperationFailedException {

        try (ProgressReporterMultiple prm = new ProgressReporterMultiple(progressReporter, 2)) {

            NamedChannelsForSeries namedChannels =
                    in.createChannelsForSeries(seriesNum, new ProgressReporterOneOfMany(prm));
            prm.incrWorker();

            return new TimeSequence( stackFromNamedChannels(namedChannels, prm) );

        } catch (RasterIOException e) {
            throw new OperationFailedException(e);
        }
    }
            
    private Stack stackFromNamedChannels(NamedChannelsForSeries namedChannels, ProgressReporterMultiple progressReporter) throws OperationFailedException {
        if (channelName.isPresent()) {
            try {
                Channel channel =
                        namedChannels.getChannel(channelName.get(), timeIndex, new ProgressReporterOneOfMany(progressReporter));
                return new Stack(channel);
            } catch (GetOperationFailedException e) {
                throw new OperationFailedException(e);
            }
        } else {
            return namedChannels.allChannelsAsStack(timeIndex).get();
        }
    }
}