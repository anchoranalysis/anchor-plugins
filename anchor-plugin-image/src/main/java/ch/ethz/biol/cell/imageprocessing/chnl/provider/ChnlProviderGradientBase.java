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

package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverter;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverterToUnsignedByte;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverterToUnsignedShort;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeFloat;

public abstract class ChnlProviderGradientBase extends ChnlProviderOne {

    // START BEAN
    @BeanField @Getter @Setter private double scaleFactor = 1.0;

    /** Iff true, outputs a short channel, otherwise byte channel */
    @BeanField @Getter @Setter private boolean outputShort = false;

    /** Added to all gradients (so we can store negative gradients) */
    @BeanField @Getter @Setter private int addSum = 0;
    // END BEAN

    @Override
    public Channel createFromChnl(Channel chnlIn) throws CreateException {

        // The gradient is calculated on a float
        Channel chnlIntermediate =
                ChannelFactory.instance()
                        .createEmptyInitialised(
                                chnlIn.getDimensions(), VoxelDataTypeFloat.INSTANCE);

        GradientCalculator calculator =
                new GradientCalculator(createDimensionArr(), (float) scaleFactor, addSum);
        calculator.calculateGradient(
                chnlIn.getVoxelBox(), chnlIntermediate.getVoxelBox().asFloat());

        return convertToOutputType(chnlIntermediate);
    }

    protected abstract boolean[] createDimensionArr() throws CreateException;

    private Channel convertToOutputType(Channel chnlToConvert) {
        ChannelConverter<?> converter =
                outputShort
                        ? new ChannelConverterToUnsignedShort()
                        : new ChannelConverterToUnsignedByte();
        return converter.convert(chnlToConvert, ConversionPolicy.CHANGE_EXISTING_CHANNEL);
    }
}
