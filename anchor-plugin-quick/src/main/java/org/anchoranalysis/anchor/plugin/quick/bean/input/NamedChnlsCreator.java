/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.input;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapCreator;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapEntry;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.plugin.io.bean.chnl.map.ImgChnlMapDefine;
import org.anchoranalysis.plugin.io.bean.input.chnl.NamedChnls;

/** Helps in creating NamedChnls */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class NamedChnlsCreator {

    public static NamedChnls create(
            InputManager<FileInput> files,
            String mainChnlName,
            int mainChnlIndex,
            List<ImgChnlMapEntry> additionalChnls,
            RasterReader rasterReader)
            throws BeanMisconfiguredException {
        NamedChnls namedChnls = new NamedChnls();
        namedChnls.setImgChnlMapCreator(
                createMapCreator(mainChnlName, mainChnlIndex, additionalChnls));
        namedChnls.setFileInput(files);
        namedChnls.setRasterReader(rasterReader);
        return namedChnls;
    }

    private static ImgChnlMapCreator createMapCreator(
            String mainChnlName, int mainChnlIndex, List<ImgChnlMapEntry> additionalChnls)
            throws BeanMisconfiguredException {
        ImgChnlMapDefine define = new ImgChnlMapDefine();
        define.setList(listEntries(mainChnlName, mainChnlIndex, additionalChnls));
        return define;
    }

    private static List<ImgChnlMapEntry> listEntries(
            String mainChnlName, int mainChnlIndex, List<ImgChnlMapEntry> additionalChnls)
            throws BeanMisconfiguredException {
        List<ImgChnlMapEntry> out = new ArrayList<>();
        addChnlEntry(out, mainChnlName, mainChnlIndex);

        for (ImgChnlMapEntry entry : additionalChnls) {

            if (entry.getIndex() == mainChnlIndex) {
                throw new BeanMisconfiguredException(
                        String.format(
                                "Channel '%s' for index %d is already defined as the main channel. There cannot be an additional channel.",
                                mainChnlName, mainChnlIndex));
            }

            addChnlEntry(out, entry.getName(), entry.getIndex());
        }
        return out;
    }

    private static void addChnlEntry(List<ImgChnlMapEntry> list, String name, int index) {
        list.add(new ImgChnlMapEntry(name, index));
    }
}
