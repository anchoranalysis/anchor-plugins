/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.image.bean.channel.convert;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.image.bean.chnl.converter.ConvertChannelTo;
import org.anchoranalysis.image.channel.converter.ChannelConverter;
import org.anchoranalysis.image.channel.converter.ChannelConverterToUnsignedByteScaleByMinMaxValue;

/**
 * Scales by compressing a certain range of values into the 8-bit signal
 *
 * @author Owen Feehan
 */
public class ScaleByParams extends ConvertChannelTo {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private KeyValueParamsProvider params;

    @BeanField @Getter @Setter private String keyLower;

    @BeanField @Getter @Setter private String keyUpper;

    @BeanField @Getter @Setter private double scaleLower;

    @BeanField @Getter @Setter private double scaleUpper;
    // END BEAN PROPERTIES

    @Override
    public ChannelConverter<?> createConverter() throws CreateException {

        KeyValueParams kvp = params.create();

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
