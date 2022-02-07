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

import java.util.Optional;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.identifier.provider.store.NamedProviderStore;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.RGBChannelNames;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.image.io.channel.map.NamedChannelsMap;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.image.io.stack.time.TimeSeries;
import org.anchoranalysis.io.input.InputFromManagerDelegate;

/**
 * An input object that converts {@link NamedChannelsInput} to {@link StackSequenceInput}.
 *
 * @author Owen Feehan
 */
public class ConvertNamedChannelsInputToStack extends InputFromManagerDelegate<NamedChannelsInput>
        implements StackSequenceInput {

    private static final String DEFAULT_STACK_NAME = "stack";

    /** Time-index to convert */
    private int timeIndex = 0;

    /**
     * By default all channels are converted into a stack. If set, only this channel is converted
     * into a stack.
     */
    private Optional<String> channelName;

    /** Records the execution times of certain operations. */
    private final ExecutionTimeRecorder executionTimeRecorder;

    /**
     * Create with an input.
     *
     * @param input the input to convert.
     * @param executionTimeRecorder records the execution times of certain operations.
     */
    public ConvertNamedChannelsInputToStack(
            NamedChannelsInput input, ExecutionTimeRecorder executionTimeRecorder) {
        this(input, 0, Optional.empty(), executionTimeRecorder);
    }

    /**
     * Create with an input.
     *
     * @param input the input to convert.
     * @param timeIndex time-index to convert.
     * @param channelName by default all channels are converted into a stack. If set, only this
     *     channel is converted into a stack.
     * @param executionTimeRecorder records the execution times of certain operations.
     */
    public ConvertNamedChannelsInputToStack(
            NamedChannelsInput input,
            int timeIndex,
            Optional<String> channelName,
            ExecutionTimeRecorder executionTimeRecorder) {
        super(input);
        this.timeIndex = timeIndex;
        this.channelName = channelName;
        this.executionTimeRecorder = executionTimeRecorder;
    }

    @Override
    public TimeSeries createStackSequenceForSeries(int seriesIndex, Logger logger)
            throws ImageIOException {
        try {
            return convert(getDelegate(), seriesIndex, logger);
        } catch (OperationFailedException e) {
            throw new ImageIOException(e);
        }
    }

    @Override
    public void addToStoreInferNames(
            NamedProviderStore<TimeSeries> stacks, int seriesIndex, Logger logger)
            throws OperationFailedException {
        String stackName = channelName.orElse(DEFAULT_STACK_NAME);
        addConvertedInputToStacks(stackName, stacks, seriesIndex, logger);
    }

    @Override
    public void addToStoreWithName(
            String name, NamedProviderStore<TimeSeries> stacks, int seriesIndex, Logger logger)
            throws OperationFailedException {
        addConvertedInputToStacks(name, stacks, seriesIndex, logger);
    }

    @Override
    public int numberFrames() throws OperationFailedException {
        return getDelegate().numberFrames();
    }

    private TimeSeries convert(NamedChannelsInput input, int seriesIndex, Logger logger)
            throws OperationFailedException {

        try {
            NamedChannelsMap channels =
                    executionTimeRecorder.recordExecutionTime(
                            "Create channel for series",
                            () -> input.createChannelsForSeries(seriesIndex, logger));
            return executionTimeRecorder.recordExecutionTime(
                    "Derive stack from channels",
                    () -> new TimeSeries(stackFromChannels(channels, logger)));

        } catch (ImageIOException e) {
            throw new OperationFailedException(e);
        }
    }

    private void addConvertedInputToStacks(
            String name, NamedProviderStore<TimeSeries> stacks, int seriesIndex, Logger logger)
            throws OperationFailedException {
        stacks.add(name, () -> convert(getDelegate(), seriesIndex, logger));
    }

    /**
     * Derives a {@link Stack} from a {@link NamedChannelsMap} that may be RGB or grayscale or
     * neither.
     */
    private Stack stackFromChannels(NamedChannelsMap channels, Logger logger)
            throws OperationFailedException {
        try {
            if (channelName.isPresent()) {
                // Create a stack with a specific channel only
                return new Stack(extractChannel(channels, channelName.get(), logger));
            } else if (channels.isRGB() && channels.numberChannels() == 3) {
                // If it's marked as RGB and has exactly three channels, extract them into a stack,
                // in theright order.
                return buildStackFromRGBChannelNames(channels, logger);
            } else {
                // Otherwise build a stack with the channel names in arbitrary order
                return channels.allChannelsAsStack(timeIndex, logger).get();
            }
        } catch (ImageIOException | GetOperationFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    /** Builds a stack, from the expected standardized names for color channels in RGB. */
    private Stack buildStackFromRGBChannelNames(NamedChannelsMap channels, Logger logger)
            throws OperationFailedException {
        try {
            return new Stack(
                    true,
                    extractChannel(channels, RGBChannelNames.RED, logger),
                    extractChannel(channels, RGBChannelNames.GREEN, logger),
                    extractChannel(channels, RGBChannelNames.BLUE, logger));
        } catch (IncorrectImageSizeException | CreateException e) {
            throw new AnchorImpossibleSituationException();
        } catch (GetOperationFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    /** Extracts a single channel with the name {@code channelName}. */
    private Channel extractChannel(NamedChannelsMap channels, String channelName, Logger logger)
            throws GetOperationFailedException {
        return channels.getChannel(channelName, timeIndex, logger);
    }
}
