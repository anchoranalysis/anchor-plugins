/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.chnl.map;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapCreator;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapEntry;
import org.anchoranalysis.image.io.chnl.map.ImgChnlMap;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;

public class ImgChnlMapDefine extends ImgChnlMapCreator {

    // START BEAN PROPERTIES
    @BeanField private List<ImgChnlMapEntry> list = new ArrayList<>();
    // END BEAN PROPERTIES

    public ImgChnlMapDefine() {
        super();
    }

    @Override
    public ImgChnlMap createMap(OpenedRaster openedRaster) {
        ImgChnlMap out = new ImgChnlMap();
        for (ImgChnlMapEntry entry : list) {
            out.add(entry);
        }
        return out;
    }

    public List<ImgChnlMapEntry> getList() {
        return list;
    }

    public void setList(List<ImgChnlMapEntry> list) {
        this.list = list;
    }
}
