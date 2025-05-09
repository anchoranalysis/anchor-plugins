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

package org.anchoranalysis.plugin.image.bean.channel.provider.score;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalProviderFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.dictionary.DictionaryProvider;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactoryUnsignedByte;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.feature.bean.VoxelScore;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsUntyped;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.math.histogram.Histogram;
import org.anchoranalysis.spatial.box.BoundingBox;

/**
 * Assigns a <i>score</i> (a value indicating how probable something is) to each voxel
 *
 * @author Owen Feehan
 */
public class ScoreVoxels extends ChannelProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChannelProvider intensity;

    @BeanField @OptionalBean @Getter @Setter private ChannelProvider gradient;

    // We don't use {@link ChannelProiderMask} as here it's optional.
    @BeanField @OptionalBean @Getter @Setter private MaskProvider mask;

    @BeanField @Getter @Setter private VoxelScore score;

    @BeanField @Getter @Setter private List<ChannelProvider> channelsExtra = Arrays.asList();

    @BeanField @Getter @Setter private List<HistogramProvider> histogramsExtra = Arrays.asList();

    @BeanField @OptionalBean @Getter @Setter private DictionaryProvider dictionary;

    // END BEAN PROPERTIES

    @Override
    public Channel get() throws ProvisionFailedException {

        Channel intensityCreated = intensity.get();

        VoxelsWrapperList voxelsCreated = createVoxelsList(intensityCreated);
        List<Histogram> histogramsCreated = ProviderBeanHelper.listFromBeans(histogramsExtra);

        Optional<Dictionary> dictionaryCreated = OptionalProviderFactory.create(dictionary);

        Optional<ObjectMask> object = createObject();

        VoxelsFromScoreCreator creator =
                new VoxelsFromScoreCreator(voxelsCreated, dictionaryCreated, histogramsCreated);

        try {
            Voxels<UnsignedByteBuffer> voxelsPixelScore =
                    creator.createVoxelsFromPixelScore(score, object);

            return new ChannelFactoryUnsignedByte()
                    .create(voxelsPixelScore, intensityCreated.resolution());
        } catch (CreateException e) {
            throw new ProvisionFailedException(e);
        }
    }

    private VoxelsWrapperList createVoxelsList(Channel channelIntensity)
            throws ProvisionFailedException {

        VoxelsWrapperList out = new VoxelsWrapperList();

        out.add(channelIntensity.voxels());

        OptionalProviderFactory.create(gradient).map(Channel::voxels).ifPresent(out::add);

        for (ChannelProvider channelProvider : channelsExtra) {
            VoxelsUntyped voxelsExtra =
                    channelProvider != null ? channelProvider.get().voxels() : null;
            out.add(voxelsExtra);
        }
        return out;
    }

    private Optional<ObjectMask> createObject() throws ProvisionFailedException {
        if (mask == null) {
            return Optional.empty();
        }

        Mask createdMask = mask.get();
        Channel channelMask = createdMask.channel();

        return Optional.of(
                new ObjectMask(
                        new BoundingBox(channelMask.extent()),
                        channelMask.voxels().asByte(),
                        createdMask.binaryValuesInt()));
    }
}
