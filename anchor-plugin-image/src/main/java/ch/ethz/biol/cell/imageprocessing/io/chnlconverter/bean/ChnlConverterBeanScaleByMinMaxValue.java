/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.io.chnlconverter.bean;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.image.bean.chnl.converter.ConvertChannelTo;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverter;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverterToUnsignedByteScaleByMinMaxValue;

/**
 * Scales by compressing a certain range of values into the 8-bit signal
 *
 * @author Owen Feehan
 */
public class ChnlConverterBeanScaleByMinMaxValue extends ConvertChannelTo {

    @BeanField @NonNegative private int min = -1;

    @BeanField @NonNegative private int max = -1;

    @Override
    public ChannelConverter<?> createConverter() {
        return new ChannelConverterToUnsignedByteScaleByMinMaxValue(min, max);
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }
}
