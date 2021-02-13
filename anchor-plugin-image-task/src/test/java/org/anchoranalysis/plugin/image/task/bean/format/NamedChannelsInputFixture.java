package org.anchoranalysis.plugin.image.task.bean.format;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.progress.Progress;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.image.io.channel.input.series.NamedChannelsForSeries;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.test.image.StackFixture;
import org.anchoranalysis.test.image.rasterwriter.ChannelSpecification;

class NamedChannelsInputFixture extends NamedChannelsInput {
    
    private final static Extent EXTENT = new Extent(200,20,3);
    
    private final Stack stack;
    
    public NamedChannelsInputFixture(ChannelSpecification channelSpecification) {
        StackFixture fixture = new StackFixture();
        this.stack = fixture.create(channelSpecification, EXTENT);
    }
    
    @Override
    public String name() {
        return "someInput";
    }

    @Override
    public Optional<Path> pathForBinding() {
        return Optional.empty();
    }

    @Override
    public int numberSeries() throws ImageIOException {
        return 1;
    }

    @Override
    public Dimensions dimensions(int seriesIndex) throws ImageIOException {
        return stack.dimensions();
    }

    @Override
    public int numberChannels() throws ImageIOException {
        return stack.getNumberChannels();
    }

    @Override
    public int bitDepth() throws ImageIOException {
        return stack.getChannel(0).getVoxelDataType().bitDepth();
    }

    @Override
    public NamedChannelsForSeries createChannelsForSeries(int seriesIndex, Progress progress)
            throws ImageIOException {
        return new NamedChannelsForSeriesFixture(stack);
    }
}