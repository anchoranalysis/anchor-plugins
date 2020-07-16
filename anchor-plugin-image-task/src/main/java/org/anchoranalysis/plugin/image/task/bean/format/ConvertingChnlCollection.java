/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.format;

import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.io.chnl.ChnlGetter;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverter;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;

class ConvertingChnlCollection implements ChnlGetter {

    private ChnlGetter source;
    private ChannelConverter<?> chnlConverter;
    private ConversionPolicy conversionPolicy;

    public ConvertingChnlCollection(
            ChnlGetter source, ChannelConverter<?> chnlConverter, ConversionPolicy changeExisting) {
        super();
        this.source = source;
        this.chnlConverter = chnlConverter;
        this.conversionPolicy = changeExisting;
    }

    @Override
    public boolean hasChnl(String chnlName) {
        return source.hasChnl(chnlName);
    }

    @Override
    public Channel getChnl(String chnlName, int t, ProgressReporter progressReporter)
            throws GetOperationFailedException {

        Channel chnl = source.getChnl(chnlName, t, progressReporter);
        return chnlConverter.convert(chnl, conversionPolicy);
    }
}
