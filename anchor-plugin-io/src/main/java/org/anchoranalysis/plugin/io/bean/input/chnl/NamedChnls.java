/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input.chnl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapCreator;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.input.NamedChnlsInputPart;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.plugin.io.bean.chnl.map.ImgChnlMapAutoname;

// Provides access to a named set of channels for each input
public class NamedChnls extends NamedChnlsBase {

    // START BEANS
    @BeanField private InputManager<FileInput> fileInput;

    @BeanField @DefaultInstance private RasterReader rasterReader;

    @BeanField private ImgChnlMapCreator imgChnlMapCreator = new ImgChnlMapAutoname();

    @BeanField private boolean useLastSeriesIndexOnly = false;
    // END BEANS

    @Override
    public List<NamedChnlsInputPart> inputObjects(InputManagerParams params)
            throws AnchorIOException {

        ArrayList<NamedChnlsInputPart> listOut = new ArrayList<>();

        Iterator<FileInput> itrFiles = fileInput.inputObjects(params).iterator();
        while (itrFiles.hasNext()) {
            listOut.add(
                    new MapPart(
                            itrFiles.next(),
                            getRasterReader(),
                            imgChnlMapCreator,
                            useLastSeriesIndexOnly));
        }

        return listOut;
    }

    public InputManager<FileInput> getFileInput() {
        return fileInput;
    }

    public void setFileInput(InputManager<FileInput> fileInput) {
        this.fileInput = fileInput;
    }

    public RasterReader getRasterReader() {
        return rasterReader;
    }

    public void setRasterReader(RasterReader rasterReader) {
        this.rasterReader = rasterReader;
    }

    public ImgChnlMapCreator getImgChnlMapCreator() {
        return imgChnlMapCreator;
    }

    public void setImgChnlMapCreator(ImgChnlMapCreator imgChnlMapCreator) {
        this.imgChnlMapCreator = imgChnlMapCreator;
    }

    public boolean isUseLastSeriesIndexOnly() {
        return useLastSeriesIndexOnly;
    }

    public void setUseLastSeriesIndexOnly(boolean useLastSeriesIndexOnly) {
        this.useLastSeriesIndexOnly = useLastSeriesIndexOnly;
    }
}
