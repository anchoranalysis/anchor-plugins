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
import org.anchoranalysis.bean.shared.dictionary.DictionaryProvider;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.image.bean.channel.ConvertChannelTo;
import org.anchoranalysis.image.core.channel.convert.ChannelConverter;
import org.anchoranalysis.image.core.channel.convert.ToUnsignedByteScaleByMinMaxValue;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;

/**
 * Scales by compressing a certain range of values into the 8-bit signal.
 *
 * @author Owen Feehan
 */
public class ScaleByDictionary extends ConvertChannelTo<UnsignedByteBuffer> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private DictionaryProvider dictionary;

    @BeanField @Getter @Setter private String keyLower;

    @BeanField @Getter @Setter private String keyUpper;

    @BeanField @Getter @Setter private double scaleLower;

    @BeanField @Getter @Setter private double scaleUpper;

    // END BEAN PROPERTIES

    @Override
    public ChannelConverter<UnsignedByteBuffer> createConverter() throws CreateException {

        try {
            Dictionary dictionaryCreated = dictionary.get();

            int min = getScaled(dictionaryCreated, keyLower, scaleLower);
            int max = getScaled(dictionaryCreated, keyUpper, scaleUpper);

            getLogger()
                    .messageLogger()
                    .logFormatted("ChannelConverter: scale with min=%d max=%d%n", min, max);

            return new ToUnsignedByteScaleByMinMaxValue(min, max);

        } catch (ProvisionFailedException e) {
            throw new CreateException(e);
        }
    }

    private static int getScaled(Dictionary dictionary, String key, double scale)
            throws CreateException {

        if (!dictionary.containsKey(key)) {
            throw new CreateException(String.format("Parameters is missing key '%s'", key));
        }

        double val = dictionary.getAsDouble(key);
        return (int) Math.round(val * scale);
    }
}
