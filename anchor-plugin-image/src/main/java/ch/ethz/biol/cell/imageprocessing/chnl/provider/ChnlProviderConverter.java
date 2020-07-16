/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.chnl.converter.ConvertChannelTo;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverter;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;

// Converts a chnl by applying a ChnlConverter. Does not need a histogram
public class ChnlProviderConverter extends ChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField private ConvertChannelTo chnlConverter;

    @BeanField
    private boolean changeExisting =
            false; // If true, the existing channel can be changed. If false, a new channel must be
    // created for any change
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {

        assert (chnl != null);

        ChannelConverter<?> converter = chnlConverter.createConverter();

        ConversionPolicy conversionPolicy =
                changeExisting
                        ? ConversionPolicy.CHANGE_EXISTING_CHANNEL
                        : ConversionPolicy.DO_NOT_CHANGE_EXISTING;
        chnl = converter.convert(chnl, conversionPolicy);
        return chnl;
    }

    public boolean isChangeExisting() {
        return changeExisting;
    }

    public void setChangeExisting(boolean changeExisting) {
        this.changeExisting = changeExisting;
    }

    public ConvertChannelTo getChnlConverter() {
        return chnlConverter;
    }

    public void setChnlConverter(ConvertChannelTo chnlConverter) {
        this.chnlConverter = chnlConverter;
    }
}
