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

package org.anchoranalysis.plugin.image.bean.stack.provider.color;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;

public class ThreeChannels extends StackProvider {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private ChannelProvider red;

    @BeanField @OptionalBean @Getter @Setter private ChannelProvider green;

    @BeanField @OptionalBean @Getter @Setter private ChannelProvider blue;
    // END BEAN PROPERTIES

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);

        if (red == null && green == null && blue == null) {
            throw new BeanMisconfiguredException(
                    "At least one of the channelProviderRed, channelProviderGreen or channelProviderBlue must be set");
        }
    }

    private static Dimensions combineWithExisting(Dimensions existing, Channel toCombine)
            throws IncorrectImageSizeException {

        if (toCombine == null) {
            return existing;
        }

        if (existing == null) {
            return toCombine.dimensions();
        }

        if (!existing.equals(toCombine.dimensions())) {
            throw new IncorrectImageSizeException("dims are not equal");
        }

        return existing;
    }

    private static Dimensions createDimensions(
            Channel channelRed, Channel channelGreen, Channel channelBlue)
            throws IncorrectImageSizeException {

        if (channelRed == null && channelGreen == null && channelBlue == null) {
            throw new IllegalArgumentException("All channels are null");
        }

        Dimensions dimensions = null;
        dimensions = combineWithExisting(dimensions, channelRed);
        dimensions = combineWithExisting(dimensions, channelGreen);
        dimensions = combineWithExisting(dimensions, channelBlue);
        return dimensions;
    }

    private static void addToStack(
            Stack stack, Channel channel, Dimensions dimensions, VoxelDataType outputChannelType)
            throws IncorrectImageSizeException, CreateException {

        if (channel == null) {
            channel = ChannelFactory.instance().create(dimensions, outputChannelType);
        }

        if (!outputChannelType.equals(channel.getVoxelDataType())) {
            throw new CreateException(
                    String.format(
                            "Channel has a different type (%s) that the expected output-type (%s)",
                            channel.getVoxelDataType(), outputChannelType));
        }

        stack.addChannel(channel);
    }

    private static String voxelDataTypeString(Channel channel) {
        if (channel != null) {
            return channel.getVoxelDataType().toString();
        } else {
            return ("empty");
        }
    }

    // Chooses the output type of the data
    private static VoxelDataType chooseOutputDataType(
            Channel channelRed, Channel channelGreen, Channel channelBlue) throws CreateException {

        VoxelDataType dataType = null;

        Channel[] all = new Channel[] {channelRed, channelGreen, channelBlue};

        for (Channel c : all) {
            if (c == null) {
                continue;
            }

            if (dataType == null) {
                dataType = c.getVoxelDataType();
            } else {
                if (!c.getVoxelDataType().equals(dataType)) {
                    String s =
                            String.format(
                                    "Input channels have different voxel data types. Red=%s; Green=%s; Blue=%s",
                                    voxelDataTypeString(channelRed),
                                    voxelDataTypeString(channelGreen),
                                    voxelDataTypeString(channelBlue));
                    throw new CreateException(s);
                }
            }
        }

        // If we have no channels, then default to unsigned 8-bit
        if (dataType == null) {
            dataType = UnsignedByteVoxelType.INSTANCE;
        }

        return dataType;
    }

    @Override
    public Stack create() throws CreateException {

        Channel channelRed = red != null ? red.create() : null;
        Channel channelGreen = green != null ? green.create() : null;
        Channel channelBlue = blue != null ? blue.create() : null;

        VoxelDataType outputType = chooseOutputDataType(channelRed, channelGreen, channelBlue);

        return createRGBStack(channelRed, channelGreen, channelBlue, outputType);
    }

    public static Stack createRGBStack(
            Channel channelRed, Channel channelGreen, Channel channelBlue, VoxelDataType outputType)
            throws CreateException {
        try {
            Dimensions dimensions = createDimensions(channelRed, channelGreen, channelBlue);

            Stack out = new Stack();
            addToStack(out, channelRed, dimensions, outputType);
            addToStack(out, channelGreen, dimensions, outputType);
            addToStack(out, channelBlue, dimensions, outputType);
            return out;
        } catch (IncorrectImageSizeException e) {
            throw new CreateException(e);
        }
    }
}
