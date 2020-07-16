/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.chnl.map;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapCreator;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapEntry;
import org.anchoranalysis.image.io.chnl.map.ImgChnlMap;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;

public class ImgChnlMapFromMetadata extends ImgChnlMapCreator {

    @Override
    public ImgChnlMap createMap(OpenedRaster openedRaster) throws CreateException {

        Optional<List<String>> names = openedRaster.channelNames();
        if (!names.isPresent()) {
            throw new CreateException("No channels names are associated with the openedRaster");
        }

        ImgChnlMap map = new ImgChnlMap();
        for (int i = 0; i < names.get().size(); i++) {
            map.add(new ImgChnlMapEntry(names.get().get(i), i));
        }

        return map;
    }
}
