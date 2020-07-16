/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.chnl.map;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapCreator;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapEntry;
import org.anchoranalysis.image.io.chnl.map.ImgChnlMap;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;

// For a raster with a single input channel
public class ImgChnlMapSingle extends ImgChnlMapCreator {

    // START BEAN PROPERTIES
    @BeanField private String chnlName = "defaultName";
    // END BEAN PROPERTIES

    public ImgChnlMapSingle() {}

    public ImgChnlMapSingle(String chnlName) {
        this.chnlName = chnlName;
    }

    @Override
    public ImgChnlMap createMap(OpenedRaster openedRaster) throws CreateException {
        ImgChnlMap map = new ImgChnlMap();
        map.add(new ImgChnlMapEntry(chnlName, 0));
        return map;
    }

    public String getChnlName() {
        return chnlName;
    }

    public void setChnlName(String chnlName) {
        this.chnlName = chnlName;
    }
}
