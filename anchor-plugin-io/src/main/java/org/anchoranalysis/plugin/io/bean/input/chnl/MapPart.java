/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input.chnl;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapCreator;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.chnl.map.ImgChnlMap;
import org.anchoranalysis.image.io.input.NamedChnlsInputPart;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeries;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeriesConcatenate;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeriesMap;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;
import org.anchoranalysis.io.input.FileInput;

/**
 * Provides a set of channels as an input, each of which has a name.
 *
 * <p>The standard implementation of {@link NamedChnlsInputPart}
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
class MapPart extends NamedChnlsInputPart {

    // START REQUIRED ARGUMENTS
    private final FileInput delegate;
    private final RasterReader rasterReader;
    private final ImgChnlMapCreator chnlMapCreator;

    /**
     * This is to correct for a problem with formats such as czi where the seriesIndex doesn't
     * indicate the total number of series but rather is incremented with each acquisition, so for
     * our purposes we treat it as if its 0
     */
    private final boolean useLastSeriesIndexOnly;
    // END REQUIRED ARGUMENTS

    // We cache a certain amount of stacks read for particular series
    private OpenedRaster openedRasterMemo = null;
    private ImgChnlMap chnlMap = null;

    @Override
    public ImageDimensions dim(int seriesIndex) throws RasterIOException {
        return openedRaster().dim(seriesIndex);
    }

    @Override
    public int numSeries() throws RasterIOException {
        if (useLastSeriesIndexOnly) {
            return 1;
        } else {
            return openedRaster().numSeries();
        }
    }

    @Override
    public boolean hasChnl(String chnlName) throws RasterIOException {
        return chnlMap().keySet().contains(chnlName);
    }

    // Where most of our time is being taken up when opening a raster
    @Override
    public NamedChnlCollectionForSeries createChnlCollectionForSeries(
            int seriesNum, ProgressReporter progressReporter) throws RasterIOException {

        // We always use the last one
        if (useLastSeriesIndexOnly) {
            seriesNum = openedRaster().numSeries() - 1;
        }

        NamedChnlCollectionForSeriesConcatenate out = new NamedChnlCollectionForSeriesConcatenate();
        out.add(new NamedChnlCollectionForSeriesMap(openedRaster(), chnlMap(), seriesNum));
        return out;
    }

    @Override
    public String descriptiveName() {
        return delegate.descriptiveName();
    }

    @Override
    public List<Path> pathForBindingForAllChannels() {
        ArrayList<Path> out = new ArrayList<>();
        pathForBinding().ifPresent(out::add);
        return out;
    }

    @Override
    public Optional<Path> pathForBinding() {
        return delegate.pathForBinding();
    }

    @Override
    public File getFile() {
        return delegate.getFile();
    }

    @Override
    public int numChnl() throws RasterIOException {
        return openedRaster().numChnl();
    }

    @Override
    public int bitDepth() throws RasterIOException {
        return openedRaster().bitDepth();
    }

    private ImgChnlMap chnlMap() throws RasterIOException {
        openedRaster();
        return chnlMap;
    }

    private OpenedRaster openedRaster() throws RasterIOException {
        if (openedRasterMemo == null) {
            openedRasterMemo =
                    rasterReader.openFile(
                            delegate.pathForBinding()
                                    .orElseThrow(
                                            () ->
                                                    new RasterIOException(
                                                            "A binding-path is needed in the delegate.")));
            try {
                chnlMap = chnlMapCreator.createMap(openedRasterMemo);
            } catch (CreateException e) {
                throw new RasterIOException(e);
            }
        }
        return openedRasterMemo;
    }

    @Override
    public void close(ErrorReporter errorReporter) {

        if (openedRasterMemo != null) {
            try {
                openedRasterMemo.close();
            } catch (RasterIOException e) {
                errorReporter.recordError(MapPart.class, e);
            }
        }
        delegate.close(errorReporter);
    }
}
