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

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInputPart;
import org.anchoranalysis.image.io.channel.map.NamedChannelsConcatenate;
import org.anchoranalysis.image.io.channel.map.NamedChannelsMap;
import org.anchoranalysis.image.io.channel.map.OpenedNamedChannels;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.io.input.path.DerivePathException;
import org.anchoranalysis.io.input.path.PathSupplier;

/**
 * Appends an additional channel to an existing {@link NamedChannelsInputPart}.
 *
 * @author Owen Feehan
 */
class AppendPart extends NamedChannelsInputPart {

    // START: REQUIRED ARGUMENTS
    /** The existing {@link NamedChannelsInputPart} to append to. */
    private final NamedChannelsInputPart toAppendTo;

    /** The additional channel to append. */
    private final AdditionalChannel additionalChannel;

    /** How to read images from the file-system. */
    private final StackReader stackReader;

    /** Recording the execution-time of each operation. */
    private final ExecutionTimeRecorder executionTimeRecorder;

    // END: REQUIRED ARGUMENTS

    /**
     * The currently opened image-file.
     *
     * <p>This is lazily opened, and is null, until first created.
     */
    private OpenedImageFile openedFile;

    public AppendPart(
            NamedChannelsInputPart toAppendTo,
            String channelName,
            int channelIndex,
            PathSupplier filePath,
            StackReader stackReader,
            ExecutionTimeRecorder executionTimeRecorder) {
        this.toAppendTo = toAppendTo;
        this.additionalChannel = new AdditionalChannel(channelName, channelIndex, filePath);
        this.stackReader = stackReader;
        this.executionTimeRecorder = executionTimeRecorder;
    }

    @Override
    public int numberSeries() throws ImageIOException {
        return toAppendTo.numberSeries();
    }

    @Override
    public Dimensions dimensions(int stackIndexInSeries, Logger logger) throws ImageIOException {
        return toAppendTo.dimensions(stackIndexInSeries, logger);
    }

    @Override
    public NamedChannelsMap createChannelsForSeries(int seriesIndex, Logger logger)
            throws ImageIOException {

        NamedChannelsMap existing = toAppendTo.createChannelsForSeries(seriesIndex, logger);

        openRasterIfNecessary();

        NamedChannelsMap opened =
                new OpenedNamedChannels(
                        openedFile, additionalChannel.createChannelMap(), seriesIndex);
        return new NamedChannelsConcatenate(existing, opened);
    }

    @Override
    public String identifier() {
        return toAppendTo.identifier();
    }

    @Override
    public Optional<Path> pathForBinding() {
        return toAppendTo.pathForBinding();
    }

    @Override
    public int numberChannels(Logger logger) throws ImageIOException {
        return toAppendTo.numberChannels(logger) + 1;
    }

    @Override
    public int bitDepth(Logger logger) throws ImageIOException {
        return toAppendTo.bitDepth(logger);
    }

    @Override
    public ImageMetadata metadata(int seriesIndex, Logger logger) throws ImageIOException {
        ImageMetadata existing = toAppendTo.metadata(seriesIndex, logger);
        return new ImageMetadata(
                existing.getDimensions(),
                existing.getNumberChannels() + 1,
                existing.getNumberFrames(),
                existing.getNumberSeries(),
                false,
                existing.getBitDepthPerChannel(),
                existing.getFileAttributes(),
                existing.getAcquisitionTime(),
                existing.getPyramid());
    }

    @Override
    public void close(ErrorReporter errorReporter) {
        if (openedFile != null) {
            try {
                openedFile.close();
            } catch (ImageIOException e) {
                errorReporter.recordError(AppendPart.class, e);
            }
        }
        toAppendTo.close(errorReporter);
    }

    private void openRasterIfNecessary() throws ImageIOException {
        try {
            Path filePathAdditional = additionalChannel.getFilePath();

            if (openedFile == null) {
                openedFile = stackReader.openFile(filePathAdditional, executionTimeRecorder);
            }

        } catch (DerivePathException e) {
            throw new ImageIOException("Failed to derive a file-path to open an image part.", e);
        }
    }
}
