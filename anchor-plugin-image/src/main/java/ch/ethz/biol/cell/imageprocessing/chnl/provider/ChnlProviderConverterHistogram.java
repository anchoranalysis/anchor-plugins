/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.chnl.converter.ConvertChannelToWithHistogram;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;
import org.anchoranalysis.image.stack.region.chnlconverter.attached.ChnlConverterAttached;

// Converts a chnl by applying a ChnlConverter. A histogram is used to determine the conversion.
// This is either
// calculated automatically from the incoming channel, or provided explicitly.
public class ChnlProviderConverterHistogram extends ChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter
    private HistogramProvider
            histogramProvider; // If null, the histogram is calculated from the image

    @BeanField @Getter @Setter private ConvertChannelToWithHistogram chnlConverter;

    @BeanField @Getter @Setter
    private boolean changeExisting =
            false; // If true, the existing channel can be changed. If false, a new channel must be
    // created for any change
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {

        Histogram hist;
        if (histogramProvider != null) {
            hist = histogramProvider.create();
        } else {
            hist = HistogramFactory.create(chnl);
        }

        ChnlConverterAttached<Histogram, ?> converter = chnlConverter.createConverter();

        try {
            converter.attachObject(hist);
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }

        ConversionPolicy conversionPolicy =
                changeExisting
                        ? ConversionPolicy.CHANGE_EXISTING_CHANNEL
                        : ConversionPolicy.DO_NOT_CHANGE_EXISTING;
        chnl = converter.convert(chnl, conversionPolicy);
        return chnl;
    }
}
