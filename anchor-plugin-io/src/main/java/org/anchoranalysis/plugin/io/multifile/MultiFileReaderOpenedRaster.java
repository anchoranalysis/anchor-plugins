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
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.image.dimensions.Dimensions;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.StackReader;
import org.anchoranalysis.image.io.stack.OpenedRaster;
import org.anchoranalysis.image.stack.TimeSequence;

// Ignores multiple series
@RequiredArgsConstructor
public class MultiFileReaderOpenedRaster implements OpenedRaster {

    // START REQUIRED ARGUMENTS
    private final StackReader stackReader;
    private final ParsedFilePathBag fileBag;
    // END REQUIRED ARGUMENTS

    // Processed version of the file. If null, not set yet
    private MultiFile multiFileMemo = null;

    @Override
    public int numberSeries() {
        // For now we only support a single series, this could be changed
        return 1;
    }

    @Override
    public TimeSequence open(int seriesIndex, ProgressReporter progressReporter)
            throws ImageIOException {

        try {
            progressReporter.open();
            return getOrCreateMemo(progressReporter).createSequence();

        } finally {
            progressReporter.close();
        }
    }

    @Override
    public Optional<List<String>> channelNames() {
        return Optional.empty();
    }

    @Override
    public int numberChannels() throws ImageIOException {
        MultiFile memo = getOrCreateMemo(ProgressReporterNull.get());

        if (!memo.numChannelDefined()) {
            throw new ImageIOException("Number of channel is not defined");
        }

        return memo.numChannel();
    }

    @Override
    public int bitDepth() throws ImageIOException {
        MultiFile memo = getOrCreateMemo(ProgressReporterNull.get());
        return memo.dataType().numberBits();
    }

    @Override
    public boolean isRGB() throws ImageIOException {
        return false;
    }

    @Override
    public int numberFrames() throws ImageIOException {

        MultiFile multiFile = getOrCreateMemo(ProgressReporterNull.get());

        if (!multiFile.numFramesDefined()) {
            throw new ImageIOException("Number of frames is not defined");
        }

        return multiFile.numFrames();
    }

    @Override
    public void close() throws ImageIOException {
        // NOTHING TO DO
    }

    @Override
    public Dimensions dimensionsForSeries(int seriesIndex) throws ImageIOException {
        throw new ImageIOException("MultiFileReader doesn't support this operation");
    }

    private void addDetailsFromBag(
            MultiFile multiFile, int seriesIndex, ProgressReporter progressReporter)
            throws ImageIOException {

        for (FileDetails fd : fileBag) {

            OpenedRaster or = stackReader.openFile(fd.getFilePath());
            try {
                TimeSequence ts = or.open(seriesIndex, progressReporter);
                multiFile.add(
                        ts.get(0),
                        fd.getChannelNum(),
                        fd.getSliceNum(),
                        fd.getTimeIndex(),
                        fd.getFilePath());
            } catch (Exception e) {
                throw new ImageIOException(
                        String.format(
                                "Could not open '%s'. Abandoning MultiFile.", fd.getFilePath()),
                        e);
            } finally {
                or.close();
            }
        }
    }

    private MultiFile getOrCreateMemo(ProgressReporter progressReporter) throws ImageIOException {
        if (multiFileMemo == null) {
            multiFileMemo = new MultiFile(fileBag);
            addDetailsFromBag(multiFileMemo, 0, progressReporter);
        }
        return multiFileMemo;
    }
}
