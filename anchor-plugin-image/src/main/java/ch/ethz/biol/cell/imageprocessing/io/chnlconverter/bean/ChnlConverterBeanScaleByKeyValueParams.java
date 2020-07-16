/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.io.chnlconverter.bean;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.image.bean.chnl.converter.ConvertChannelTo;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverter;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverterToUnsignedByteScaleByMinMaxValue;

/**
 * Scales by compressing a certain range of values into the 8-bit signal
 *
 * @author Owen Feehan
 */
public class ChnlConverterBeanScaleByKeyValueParams extends ConvertChannelTo {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private KeyValueParamsProvider keyValueParamsProvider;

    @BeanField @Getter @Setter private String keyLower;

    @BeanField @Getter @Setter private String keyUpper;

    @BeanField @Getter @Setter private double scaleLower;

    @BeanField @Getter @Setter private double scaleUpper;
    // END BEAN PROPERTIES

    @Override
    public ChannelConverter<?> createConverter() throws CreateException {

        KeyValueParams kvp = keyValueParamsProvider.create();

        int min = getScaled(kvp, keyLower, scaleLower);
        int max = getScaled(kvp, keyUpper, scaleUpper);

        getLogger()
                .messageLogger()
                .logFormatted("ChnlConverter: scale with min=%d max=%d%n", min, max);

        return new ChannelConverterToUnsignedByteScaleByMinMaxValue(min, max);
    }

    private int getScaled(KeyValueParams kvp, String key, double scale) throws CreateException {

        if (!kvp.containsKey(key)) {
            throw new CreateException(String.format("Params is missing key '%s'", key));
        }

        double val = kvp.getPropertyAsDouble(key);

        getLogger().messageLogger().logFormatted("%f * %f = %f", val, scale, val * scale);

        return (int) Math.round(val * scale);
    }
}
