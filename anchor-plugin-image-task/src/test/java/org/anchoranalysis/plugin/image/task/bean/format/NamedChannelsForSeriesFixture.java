package org.anchoranalysis.plugin.image.task.bean.format;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.provider.store.NamedProviderStore;
import org.anchoranalysis.core.identifier.provider.store.StoreSupplier;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.progress.Progress;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.RGBChannelNames;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.core.stack.TimeSequence;
import org.anchoranalysis.image.core.stack.named.NamedStacks;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.channel.input.series.NamedChannelsForSeries;

/**
 * Exposes a {@link NamedChannelsForSeries} with a certain number of channels.
 *
 * <p>The channels are named: {@code channel00, channel01, channel02} etc. unless it's an RGB-stack,
 * in which case they are named {@code red, green, blue}.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
class NamedChannelsForSeriesFixture implements NamedChannelsForSeries {

    private static final Pattern PATTERN = Pattern.compile("^channel(\\d\\d)$");

    private final Stack stack;

    @Override
    public boolean hasChannel(String channelName) {
        if (stack.isRGB()) {
            return RGBChannelNames.isValidName(channelName);
        } else {
            return PATTERN.matcher(channelName).matches();
        }
    }

    @Override
    public Channel getChannel(String channelName, int timeIndex, Progress progress)
            throws GetOperationFailedException {

        return getChannelOptional(channelName, timeIndex, progress)
                .orElseThrow(
                        () ->
                                new GetOperationFailedException(
                                        channelName, "Channel does not exist"));
    }

    @Override
    public int sizeT(Progress progress) throws ImageIOException {
        return 1;
    }

    @Override
    public int numberChannels() {
        return stack.getNumberChannels();
    }

    @Override
    public boolean isRGB() throws ImageIOException {
        return stack.isRGB();
    }

    @Override
    public Optional<Channel> getChannelOptional(
            String channelName, int timeIndex, Progress progress)
            throws GetOperationFailedException {
        if (timeIndex == 0) {
            if (stack.isRGB()) {
                return getChannelRGB(channelName);
            } else {
                return getChannelNumeric(channelName);
            }
        } else {
            throw new GetOperationFailedException(channelName, "Only timeIndex==0 is supported");
        }
    }

    @Override
    public Dimensions dimensions() throws ImageIOException {
        return stack.dimensions();
    }

    @Override
    public Set<String> channelNames() {
        if (stack.isRGB()) {
            return RGBChannelNames.asSet();
        } else {
            return channelNamesNumeric(numberChannels());
        }
    }

    @Override
    public StoreSupplier<Stack> allChannelsAsStack(int timeIndex) {
        return StoreSupplier.cache(() -> stack);
    }

    @Override
    public void addAsSeparateChannels(NamedProviderStore<TimeSequence> stacks, int timeIndex)
            throws OperationFailedException {
        throwUnsupportedException();
    }

    @Override
    public void addAsSeparateChannels(NamedStacks stacks, int timeIndex, Progress progress)
            throws OperationFailedException {
        throwUnsupportedException();
    }

    private Optional<Channel> getChannelRGB(String channelName) {
        return RGBChannelNames.deriveIndex(channelName).map(index -> stack.getChannel(index));
    }

    private Optional<Channel> getChannelNumeric(String channelName) {
        Matcher matcher = PATTERN.matcher(channelName);
        return OptionalFactory.create(matcher.matches(), () -> getChannel(matcher.group(1)));
    }

    private Channel getChannel(String indexAsString) {
        int channelIndex = Integer.parseInt(indexAsString);
        return stack.getChannel(channelIndex);
    }

    private void throwUnsupportedException() throws OperationFailedException {
        throw new OperationFailedException("Unsupported Operation");
    }

    private static Set<String> channelNamesNumeric(int numberChannels) {
        return IntStream.range(0, numberChannels)
                .mapToObj(index -> String.format("channel%02d", index))
                .collect(Collectors.toSet());
    }
}
