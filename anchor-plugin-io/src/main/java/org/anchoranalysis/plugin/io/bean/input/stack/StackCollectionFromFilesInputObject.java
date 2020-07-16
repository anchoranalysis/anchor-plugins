/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input.stack;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.name.store.NamedProviderStore;
import org.anchoranalysis.core.progress.OperationWithProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;
import org.anchoranalysis.image.stack.TimeSequence;
import org.anchoranalysis.io.input.FileInput;

class StackCollectionFromFilesInputObject extends StackSequenceInput {

    private FileInput delegate;
    private RasterReader rasterReader;

    // We cache a certain amount of stacks read for particular series
    private OpenedRaster openedRasterMemo = null;

    // This is to correct for a problem with formats such as czi where the seriesIndex doesn't
    // indicate
    //   the total number of series but rather is incremented with each acquisition, so for our
    // purposes
    //   we treat it as if its 0
    private boolean useLastSeriesIndexOnly = false;

    // The root object that is used to provide the descriptiveName and pathForBinding
    public StackCollectionFromFilesInputObject(
            FileInput delegate, RasterReader rasterReader, boolean useLastSeriesIndexOnly) {
        super();
        assert (rasterReader != null);
        this.delegate = delegate;
        this.rasterReader = rasterReader;
        this.useLastSeriesIndexOnly = useLastSeriesIndexOnly;
    }

    public int numSeries() throws RasterIOException {
        if (useLastSeriesIndexOnly) {
            return 1;
        } else {
            return getOpenedRaster().numSeries();
        }
    }

    @Override
    public int numFrames() throws OperationFailedException {

        try {
            return getOpenedRaster().numFrames();
        } catch (RasterIOException e) {
            throw new OperationFailedException(e);
        }
    }

    public OperationWithProgressReporter<TimeSequence, OperationFailedException>
            createStackSequenceForSeries(int seriesNum) throws RasterIOException {

        // We always use the last one
        if (useLastSeriesIndexOnly) {
            seriesNum = getOpenedRaster().numSeries() - 1;
        }
        return openRasterAsOperation(getOpenedRaster(), seriesNum);
    }

    @Override
    public void addToStore(
            NamedProviderStore<TimeSequence> stackCollection,
            int seriesNum,
            ProgressReporter progressReporter)
            throws OperationFailedException {
        throw new OperationFailedException("Not supported");
    }

    @Override
    public void addToStoreWithName(
            String name,
            NamedProviderStore<TimeSequence> stackCollection,
            int seriesNum,
            ProgressReporter progressReporter)
            throws OperationFailedException {

        stackCollection.add(
                name,
                () -> {
                    try {
                        return createStackSequenceForSeries(seriesNum)
                                .doOperation(progressReporter);
                    } catch (RasterIOException e) {
                        throw new OperationFailedException(e);
                    }
                });
    }

    private static OperationWithProgressReporter<TimeSequence, OperationFailedException>
            openRasterAsOperation(final OpenedRaster openedRaster, final int seriesNum) {
        return pr -> {
            try {
                return openedRaster.open(seriesNum, pr);
            } catch (RasterIOException e) {
                throw new OperationFailedException(e);
            }
        };
    }

    @Override
    public String descriptiveName() {
        return delegate.descriptiveName();
    }

    @Override
    public Optional<Path> pathForBinding() {
        return delegate.pathForBinding();
    }

    public RasterReader getRasterReader() {
        return rasterReader;
    }

    public File getFile() {
        return delegate.getFile();
    }

    private OpenedRaster getOpenedRaster() throws RasterIOException {
        if (openedRasterMemo == null) {
            openedRasterMemo =
                    rasterReader.openFile(
                            delegate.pathForBinding()
                                    .orElseThrow(
                                            () ->
                                                    new RasterIOException(
                                                            "A binding-path must be associated with this file")));
        }
        return openedRasterMemo;
    }

    @Override
    public void close(ErrorReporter errorReporter) {
        if (openedRasterMemo != null) {
            try {
                openedRasterMemo.close();
            } catch (RasterIOException e) {
                errorReporter.recordError(StackSequenceInput.class, e);
            }
        }
    }
}
