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
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;

/**
 * Provides a {@link Stack} by combining up to three channels (red, green, blue) into an RGB image.
 */
public class ThreeChannels extends StackProvider {

    // START BEAN PROPERTIES
    /** Provider for the red channel. */
    @BeanField @OptionalBean @Getter @Setter private ChannelProvider red;

    /** Provider for the green channel. */
    @BeanField @OptionalBean @Getter @Setter private ChannelProvider green;

    /** Provider for the blue channel. */
    @BeanField @OptionalBean @Getter @Setter private ChannelProvider blue;
    // END BEAN PROPERTIES

    // The checkMisconfigured and get methods are overridden, so we don't add doc-strings for them

    /**
     * Combines the dimensions of an existing channel with a new channel to combine.
     *
     * @param existing the existing dimensions, or null if not yet set.
     * @param toCombine the channel to combine, which may be null.
     * @return the combined dimensions.
     * @throws IncorrectImageSizeException if the dimensions don't match.
     */
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

    /**
     * Creates dimensions from up to three channels.
     *
     * @param channelRed the red channel, which may be null.
     * @param channelGreen the green channel, which may be null.
     * @param channelBlue the blue channel, which may be null.
     * @return the combined dimensions.
     * @throws IncorrectImageSizeException if the dimensions don't match.
     */
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

    /**
     * Adds a channel to a stack, creating an empty channel if necessary.
     *
     * @param stack the {@link Stack} to add the channel to.
     * @param channel the {@link Channel} to add, which may be null.
     * @param dimensions the dimensions for the channel.
     * @param outputChannelType the expected voxel data type for the channel.
     * @throws IncorrectImageSizeException if the dimensions don't match.
     * @throws ProvisionFailedException if the channel has an unexpected voxel data type.
     */
    private static void addToStack(
            Stack stack, Channel channel, Dimensions dimensions, VoxelDataType outputChannelType)
            throws IncorrectImageSizeException, ProvisionFailedException {

        if (channel == null) {
            channel = ChannelFactory.instance().create(dimensions, outputChannelType);
        }

        if (!outputChannelType.equals(channel.getVoxelDataType())) {
            throw new ProvisionFailedException(
                    String.format(
                            "Channel has a different type (%s) that the expected output-type (%s)",
                            channel.getVoxelDataType(), outputChannelType));
        }

        stack.addChannel(channel);
    }

    /**
     * Gets a string representation of a channel's voxel data type.
     *
     * @param channel the {@link Channel} to get the voxel data type from, which may be null.
     * @return a string representation of the voxel data type, or "empty" if the channel is null.
     */
    private static String voxelDataTypeString(Channel channel) {
        if (channel != null) {
            return channel.getVoxelDataType().toString();
        } else {
            return ("empty");
        }
    }

    /**
     * Chooses the output voxel data type based on the input channels.
     *
     * @param channelRed the red channel, which may be null.
     * @param channelGreen the green channel, which may be null.
     * @param channelBlue the blue channel, which may be null.
     * @return the chosen {@link VoxelDataType} for output.
     * @throws ProvisionFailedException if the input channels have different voxel data types.
     */
    private static VoxelDataType chooseOutputDataType(
            Channel channelRed, Channel channelGreen, Channel channelBlue)
            throws ProvisionFailedException {

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
                    throw new ProvisionFailedException(s);
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
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);

        if (red == null && green == null && blue == null) {
            throw new BeanMisconfiguredException(
                    "At least one of the channelProviderRed, channelProviderGreen or channelProviderBlue must be set");
        }
    }

    @Override
    public Stack get() throws ProvisionFailedException {

        Channel channelRed = red != null ? red.get() : null;
        Channel channelGreen = green != null ? green.get() : null;
        Channel channelBlue = blue != null ? blue.get() : null;

        VoxelDataType outputType = chooseOutputDataType(channelRed, channelGreen, channelBlue);

        return createRGBStack(channelRed, channelGreen, channelBlue, outputType);
    }

    /**
     * Creates an RGB stack from up to three channels.
     *
     * @param channelRed the red channel, which may be null.
     * @param channelGreen the green channel, which may be null.
     * @param channelBlue the blue channel, which may be null.
     * @param outputType the {@link VoxelDataType} for the output channels.
     * @return a new {@link Stack} containing the RGB channels.
     * @throws ProvisionFailedException if the stack cannot be created.
     */
    public static Stack createRGBStack(
            Channel channelRed, Channel channelGreen, Channel channelBlue, VoxelDataType outputType)
            throws ProvisionFailedException {
        try {
            Dimensions dimensions = createDimensions(channelRed, channelGreen, channelBlue);

            Stack out = new Stack();
            addToStack(out, channelRed, dimensions, outputType);
            addToStack(out, channelGreen, dimensions, outputType);
            addToStack(out, channelBlue, dimensions, outputType);
            return out;
        } catch (IncorrectImageSizeException e) {
            throw new ProvisionFailedException(e);
        }
    }
}