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

package org.anchoranalysis.plugin.io.bean.input.stack;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.provider.store.NamedProviderStore;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.core.progress.Progress;
import org.anchoranalysis.image.core.stack.TimeSequence;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.image.io.stack.input.TimeSequenceSupplier;
import org.anchoranalysis.io.input.file.FileInput;

@RequiredArgsConstructor
class StackCollectionFromFilesInputObject implements StackSequenceInput {

    /** The root object that is used to provide the input-name and {@code pathForBinding}. */
    private final FileInput delegate;

    @Getter private final StackReader stackReader;

    /**
     * Uses the last series (from all series) only, and ignores any other series-numbers
     *
     * <p>This is to correct for a problem with formats such as czi where the seriesIndex doesn't
     * indicate the total number of series but rather is incremented with each acquisition, so for
     * our purposes we treat it as if its 0.
     */
    private final boolean useLastSeriesIndexOnly;

    // We cache a certain amount of stacks read for particular series
    private OpenedImageFile openedFileMemo;

    public int numberSeries() throws ImageIOException {
        if (useLastSeriesIndexOnly) {
            return 1;
        } else {
            return getOpenedRaster().numberSeries();
        }
    }

    @Override
    public int numberFrames() throws OperationFailedException {

        try {
            return getOpenedRaster().numberFrames();
        } catch (ImageIOException e) {
            throw new OperationFailedException(e);
        }
    }

    public TimeSequenceSupplier createStackSequenceForSeries(int seriesIndex)
            throws ImageIOException {

        // We always use the last one
        if (useLastSeriesIndexOnly) {
            seriesIndex = getOpenedRaster().numberSeries() - 1;
        }
        return openRasterAsOperation(getOpenedRaster(), seriesIndex);
    }

    @Override
    public void addToStoreInferNames(
            NamedProviderStore<TimeSequence> stackCollection, int seriesIndex, Progress progress)
            throws OperationFailedException {
        throw new OperationFailedException("Not supported");
    }

    @Override
    public void addToStoreWithName(
            String name,
            NamedProviderStore<TimeSequence> stacks,
            int seriesIndex,
            Progress progress)
            throws OperationFailedException {

        stacks.add(
                name,
                () -> {
                    try {
                        return createStackSequenceForSeries(seriesIndex).get(progress);
                    } catch (ImageIOException e) {
                        throw new OperationFailedException(e);
                    }
                });
    }

    @Override
    public String identifier() {
        return delegate.identifier();
    }

    @Override
    public Optional<Path> pathForBinding() {
        return delegate.pathForBinding();
    }

    public File getFile() {
        return delegate.getFile();
    }

    @Override
    public void close(ErrorReporter errorReporter) {
        if (openedFileMemo != null) {
            try {
                openedFileMemo.close();
            } catch (ImageIOException e) {
                errorReporter.recordError(StackSequenceInput.class, e);
            }
        }
    }

    private OpenedImageFile getOpenedRaster() throws ImageIOException {
        if (openedFileMemo == null) {
            openedFileMemo =
                    stackReader.openFile(
                            delegate.pathForBinding()
                                    .orElseThrow(
                                            () ->
                                                    new ImageIOException(
                                                            "A binding-path must be associated with this file")));
        }
        return openedFileMemo;
    }

    private static TimeSequenceSupplier openRasterAsOperation(
            OpenedImageFile openedFile, int seriesIndex) {
        return progress -> {
            try {
                return openedFile.open(seriesIndex, progress);
            } catch (ImageIOException e) {
                throw new OperationFailedException(e);
            }
        };
    }
}
