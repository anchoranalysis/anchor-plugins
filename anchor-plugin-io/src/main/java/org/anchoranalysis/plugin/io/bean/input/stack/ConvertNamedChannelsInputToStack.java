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
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.identifier.provider.store.NamedProviderStore;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.core.progress.Progress;
import org.anchoranalysis.core.progress.ProgressMultiple;
import org.anchoranalysis.core.progress.ProgressOneOfMany;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.RGBChannelNames;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.core.stack.TimeSequence;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.image.io.channel.input.series.NamedChannelsForSeries;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.image.io.stack.input.TimeSequenceSupplier;

/**
 * An input object that converts {@link NamedChannelsInput} to {@link StackSequenceInput}
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
public class ConvertNamedChannelsInputToStack implements StackSequenceInput {

    private static final String DEFAULT_STACK_NAME = "stack";
    
    /** Input to convert */
    private NamedChannelsInput input;

    /** Time-index to convert */
    private int timeIndex = 0;

    /**
     * By default all channels are converted into a stack. If set, only this channel is converted
     * into a stack.
     */
    private Optional<String> channelName;

    public ConvertNamedChannelsInputToStack(NamedChannelsInput input) {
        this(input, 0, Optional.empty());
    }

    @Override
    public String name() {
        return input.name();
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
    public TimeSequenceSupplier createStackSequenceForSeries(int seriesIndex)
            throws ImageIOException {
        return progress -> convert(progress, input, seriesIndex);
    }

    @Override
    public void addToStoreInferNames(
            NamedProviderStore<TimeSequence> stacks,
            int seriesIndex,
            Progress progress)
            throws OperationFailedException {
        String stackName = channelName.orElse(DEFAULT_STACK_NAME);
        addConvertedInputToStacks(stackName, stacks, seriesIndex, progress);
    }

    @Override
    public void addToStoreWithName(
            String name,
            NamedProviderStore<TimeSequence> stacks,
            int seriesIndex,
            Progress progress)
            throws OperationFailedException {
        addConvertedInputToStacks(name, stacks, seriesIndex, progress);
    }

    @Override
    public int numberFrames() throws OperationFailedException {
        return input.numberFrames();
    }

    private TimeSequence convert(
            Progress progress, NamedChannelsInput input, int seriesIndex)
            throws OperationFailedException {

        try (ProgressMultiple progressMultiple = new ProgressMultiple(progress, 2)) {

            NamedChannelsForSeries channels =
                    input.createChannelsForSeries(seriesIndex, new ProgressOneOfMany(progressMultiple));
            progressMultiple.incrementWorker();

            return new TimeSequence(stackFromChannels(channels, progressMultiple));

        } catch (ImageIOException e) {
            throw new OperationFailedException(e);
        }
    }
        
    private void addConvertedInputToStacks(String name, NamedProviderStore<TimeSequence> stacks, int seriesIndex, Progress progress) throws OperationFailedException {
        stacks.add(name, () -> convert(progress, input, seriesIndex) );
    }

    private Stack stackFromChannels(
            NamedChannelsForSeries channels, ProgressMultiple progress)
            throws OperationFailedException {
        try {
            if (channelName.isPresent()) {
                return new Stack( extractChannel(channels, channelName.get(), progress) );
            } else if (channels.isRGB()) {
                return rgbStackFromChannels(channels, progress);
            } else {
                return channels.allChannelsAsStack(timeIndex).get();
            }
        } catch (ImageIOException | GetOperationFailedException e) {
            throw new OperationFailedException(e);
        }
    }
    
    private Stack rgbStackFromChannels(NamedChannelsForSeries channels, ProgressMultiple progress) throws OperationFailedException {
        if (channels.numberChannels() !=3 ) {
            throw new OperationFailedException("There must be exactly 3 channels for a RGB stack, but there are " + channels.numberChannels());
        }
        
        try {
            return new Stack(true, extractChannel(channels, RGBChannelNames.RED,progress), extractChannel(channels,RGBChannelNames.GREEN,progress), extractChannel(channels,RGBChannelNames.BLUE,progress) );
        } catch (IncorrectImageSizeException e) {
            throw new AnchorImpossibleSituationException();
        } catch (GetOperationFailedException e) {
            throw new OperationFailedException(e);
        }
    }
    
    private Channel extractChannel(NamedChannelsForSeries channels, String channelName, ProgressMultiple progress) throws GetOperationFailedException {
        return channels.getChannel(channelName, timeIndex, new ProgressOneOfMany(progress));
    }
}
