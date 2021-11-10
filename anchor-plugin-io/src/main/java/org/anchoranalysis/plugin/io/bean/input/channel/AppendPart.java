/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.bean.input.channel;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.core.progress.Progress;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInputPart;
import org.anchoranalysis.image.io.channel.input.series.NamedChannelsForSeries;
import org.anchoranalysis.image.io.channel.input.series.NamedChannelsForSeriesConcatenate;
import org.anchoranalysis.image.io.channel.input.series.NamedChannelsForSeriesMap;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.io.input.path.DerivePathException;
import org.anchoranalysis.io.input.path.PathSupplier;

/**
 * Appends an additional channel to an existing {@link NamedChannelsInputPart}.
 *
 * @author Owen Feehan
 */
class AppendPart extends NamedChannelsInputPart {

    /** The existing {@link NamedChannelsInputPart} to append to. */
    private final NamedChannelsInputPart toAppendTo;

    /** The additional channel to append. */
    private final AdditionalChannel additionalChannel;

    private final StackReader stackReader;

    private OpenedImageFile openedFileMemo;

    public AppendPart(
            NamedChannelsInputPart toAppendTo,
            String channelName,
            int channelIndex,
            PathSupplier filePath,
            StackReader stackReader) {
        this.toAppendTo = toAppendTo;
        this.additionalChannel = new AdditionalChannel(channelName, channelIndex, filePath);
        this.stackReader = stackReader;
    }

    @Override
    public int numberSeries() throws ImageIOException {
        return toAppendTo.numberSeries();
    }

    @Override
    public Dimensions dimensions(int stackIndexInSeries) throws ImageIOException {
        return toAppendTo.dimensions(stackIndexInSeries);
    }

    @Override
    public boolean hasChannel(String channelName) throws ImageIOException {

        if (additionalChannel.getName().equals(channelName)) {
            return true;
        }
        return toAppendTo.hasChannel(channelName);
    }

    @Override
    public NamedChannelsForSeries createChannelsForSeries(int seriesIndex, Progress progress)
            throws ImageIOException {

        NamedChannelsForSeries existing = toAppendTo.createChannelsForSeries(seriesIndex, progress);

        openRasterIfNecessary();

        NamedChannelsForSeriesConcatenate out = new NamedChannelsForSeriesConcatenate();
        out.add(existing);
        out.add(
                new NamedChannelsForSeriesMap(
                        openedFileMemo, additionalChannel.createChannelMap(), seriesIndex));
        return out;
    }

    @Override
    public String identifier() {
        return toAppendTo.identifier();
    }

    @Override
    public List<Path> pathForBindingForAllChannels() throws OperationFailedException {
        try {
            List<Path> list = toAppendTo.pathForBindingForAllChannels();
            list.add(additionalChannel.getFilePath());
            return list;

        } catch (DerivePathException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    public Optional<Path> pathForBinding() {
        return toAppendTo.pathForBinding();
    }

    @Override
    public File getFile() {
        return toAppendTo.getFile();
    }

    @Override
    public int numberChannels() throws ImageIOException {
        return toAppendTo.numberChannels() + 1;
    }

    @Override
    public int bitDepth() throws ImageIOException {
        return toAppendTo.bitDepth();
    }

    @Override
    public ImageMetadata metadata(int seriesIndex) throws ImageIOException {
        ImageMetadata existing = toAppendTo.metadata(seriesIndex);
        return new ImageMetadata(
                existing.getDimensions(),
                existing.getNumberChannels() + 1,
                existing.getNumberFrames(),
                false,
                existing.getBitDepthPerChannel(),
                existing.getFileAttributes(),
                existing.getAcqusitionTime());
    }

    @Override
    public void close(ErrorReporter errorReporter) {
        if (openedFileMemo != null) {
            try {
                openedFileMemo.close();
            } catch (ImageIOException e) {
                errorReporter.recordError(AppendPart.class, e);
            }
        }
        toAppendTo.close(errorReporter);
    }

    private void openRasterIfNecessary() throws ImageIOException {
        try {
            Path filePathAdditional = additionalChannel.getFilePath();

            if (openedFileMemo == null) {
                openedFileMemo = stackReader.openFile(filePathAdditional);
            }

        } catch (DerivePathException e) {
            throw new ImageIOException(e);
        }
    }
}
