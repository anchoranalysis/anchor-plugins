/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.groupfiles;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapCreator;
import org.anchoranalysis.image.io.chnl.map.ImgChnlMap;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeries;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeriesMap;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;
import org.anchoranalysis.plugin.io.multifile.MultiFileReaderOpenedRaster;

class GroupingInput extends NamedChnlsInput {

    // The opened raster with multiple files
    private OpenedRaster openedRaster = null;

    // A virtual path uniquely representing this particular file
    private Path virtualPath;

    private ImgChnlMapCreator chnlMapCreator;

    private ImgChnlMap chnlMap = null;

    private String descriptiveName;

    // The root object that is used to provide the descriptiveName and pathForBinding
    //
    public GroupingInput(
            Path virtualPath, MultiFileReaderOpenedRaster mfor, ImgChnlMapCreator chnlMapCreator) {
        super();
        this.virtualPath = virtualPath;
        this.openedRaster = mfor;
        this.chnlMapCreator = chnlMapCreator;
    }

    @Override
    public int numSeries() throws RasterIOException {
        return openedRaster.numSeries();
    }

    @Override
    public ImageDimensions dim(int seriesIndex) throws RasterIOException {
        return openedRaster.dim(seriesIndex);
    }

    @Override
    public NamedChnlCollectionForSeries createChnlCollectionForSeries(
            int seriesNum, ProgressReporter progressReporter) throws RasterIOException {
        ensureChnlMapExists();
        return new NamedChnlCollectionForSeriesMap(openedRaster, chnlMap, seriesNum);
    }

    @Override
    public String descriptiveName() {
        return descriptiveName;
    }

    @Override
    public Optional<Path> pathForBinding() {
        return Optional.of(virtualPath);
    }

    @Override
    public int numChnl() throws RasterIOException {
        ensureChnlMapExists();
        return chnlMap.keySet().size();
    }

    @Override
    public void close(ErrorReporter errorReporter) {
        try {
            openedRaster.close();
        } catch (RasterIOException e) {
            errorReporter.recordError(GroupingInput.class, e);
        }
    }

    private void ensureChnlMapExists() throws RasterIOException {
        // Lazy creation
        if (chnlMap == null) {
            try {
                chnlMap = chnlMapCreator.createMap(openedRaster);
            } catch (CreateException e) {
                throw new RasterIOException(e);
            }
        }
    }

    @Override
    public int bitDepth() throws RasterIOException {
        return openedRaster.bitDepth();
    }
}
