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

package org.anchoranalysis.plugin.image.bean.channel.provider.object;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.voxel.object.ObjectCollection;

/**
 * Creates a channel with a level calculated for each object using various methods.
 *
 * <p>This abstract class provides a base for creating channels where each object is assigned a
 * specific intensity level based on calculations performed on the input channel.
 *
 * @author Owen Feehan
 */
public abstract class LevelPerObjectBase extends ChannelProviderUnary {

    // START BEAN PROPERTIES
    /** The objects for whom a level is calculated. */
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;

    /**
     * Method to calculate the level for a particular object.
     *
     * <p>It is passed a histogram (calculated in different ways) for each object.
     */
    @BeanField @Getter @Setter private CalculateLevel calculateLevel;
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChannel(Channel channel) throws ProvisionFailedException {

        Channel output =
                ChannelFactory.instance().create(channel.dimensions(), channel.getVoxelDataType());
        writeLevelsForObjects(channel, objects.get(), output);
        return output;
    }

    /**
     * Creates a channel with the levels for a set of objects.
     *
     * @param input the channel whose intensity is passed to {@code calculateLevel} as a histogram
     *     variously for particular objects
     * @param objects the {@link ObjectCollection} for which levels are calculated
     * @param output the channel where the calculated-levels are written (for each object)
     * @throws ProvisionFailedException if there's an error during the level calculation or writing
     *     process
     */
    protected abstract void writeLevelsForObjects(
            Channel input, ObjectCollection objects, Channel output)
            throws ProvisionFailedException;
}
