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

package org.anchoranalysis.plugin.io.multifile;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.ImagePyramidMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.ImageTimestampsAttributes;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.image.io.stack.time.TimeSeries;

/**
 * A {@link OpenedImageFile} where the image is formed from more than one file on the file-system.
 *
 * <p>It ignores multiple series.
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
public class OpenedMultiFile implements OpenedImageFile {

    // START REQUIRED ARGUMENTS
    private final StackReader stackReader;
    private final ParsedFilePathBag fileBag;
    private final ExecutionTimeRecorder executionTimeRecorder;
    // END REQUIRED ARGUMENTS

    // Processed version of the file. If null, not set yet
    private MultiFile multiFileMemo = null;

    @Override
    public int numberSeries() {
        // For now we only support a single series, this could be changed
        return 1;
    }

    @Override
    public TimeSeries open(int seriesIndex, Logger logger) throws ImageIOException {
        return getOrCreateMemo(logger).createSequence();
    }

    @Override
    public Optional<List<String>> channelNames(Logger logger) {
        return Optional.empty();
    }

    @Override
    public int numberChannels(Logger logger) throws ImageIOException {
        MultiFile memo = getOrCreateMemo(logger);

        if (!memo.numChannelDefined()) {
            throw new ImageIOException("Number of channel is not defined");
        }

        return memo.numChannel();
    }

    @Override
    public int bitDepth(Logger logger) throws ImageIOException {
        MultiFile memo = getOrCreateMemo(logger);
        return memo.dataType().bitDepth();
    }

    @Override
    public boolean isRGB(Logger logger) throws ImageIOException {
        return false;
    }

    @Override
    public int numberFrames(Logger logger) throws ImageIOException {

        MultiFile multiFile = getOrCreateMemo(logger);

        if (!multiFile.numFramesDefined()) {
            throw new ImageIOException("Number of frames is not defined");
        }

        return multiFile.numFrames();
    }

    @Override
    public ImageTimestampsAttributes timestamps() throws ImageIOException {
        throw new ImageIOException(
                "Timestamps are not supported for multi-files, as it is not well-defined which file should be used.");
    }

    @Override
    public void close() throws ImageIOException {
        // NOTHING TO DO
    }

    @Override
    public Dimensions dimensionsForSeries(int seriesIndex, Logger logger) throws ImageIOException {
        throw new ImageIOException("MultiFileReader doesn't support this operation");
    }

    @Override
    public Optional<ImagePyramidMetadata> pyramid() throws ImageIOException {
        return Optional.empty();
    }

    private void addDetailsFromBag(MultiFile multiFile, int seriesIndex, Logger logger)
            throws ImageIOException {

        for (FileDetails details : fileBag) {

            OpenedImageFile imageFile =
                    stackReader.openFile(details.getPath(), executionTimeRecorder);
            try {
                TimeSeries timeSequence = imageFile.open(seriesIndex, logger);
                multiFile.add(
                        timeSequence.getFrame(0),
                        details.getChannelIndex(),
                        details.getSliceIndex(),
                        details.getTimeIndex(),
                        details.getPath());
            } catch (Exception e) {
                throw new ImageIOException(
                        String.format(
                                "Could not open '%s'. Abandoning MultiFile.", details.getPath()),
                        e);
            } finally {
                imageFile.close();
            }
        }
    }

    private MultiFile getOrCreateMemo(Logger logger) throws ImageIOException {
        if (multiFileMemo == null) {
            multiFileMemo = new MultiFile(fileBag);
            addDetailsFromBag(multiFileMemo, 0, logger);
        }
        return multiFileMemo;
    }
}
