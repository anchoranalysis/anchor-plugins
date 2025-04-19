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

package org.anchoranalysis.plugin.image.bean.object.provider.segment;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalProviderFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.bean.nonbean.segment.SegmentationFailedException;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.provider.WithChannelBase;
import org.anchoranalysis.spatial.point.Point3i;

/**
 * Segments a channel into objects using a specified segmentation algorithm.
 *
 * <p>This class extends {@link WithChannelBase} to provide functionality for segmenting
 * a channel into objects, optionally using a mask and seed objects.</p>
 */
public class SegmentChannel extends WithChannelBase {

    // START BEAN PROPERTIES
    /** Optional mask to restrict the segmentation area. */
    @BeanField @OptionalBean @Getter @Setter private MaskProvider mask;

    /** The segmentation algorithm to use. */
    @BeanField @Getter @Setter private SegmentChannelIntoObjects segment;

    /** Optional provider for seed objects to guide the segmentation. */
    @BeanField @OptionalBean @Getter @Setter private ObjectCollectionProvider objectsSeeds;
    // END BEAN PROPERTIES

    @Override
    protected ObjectCollection createFromChannel(Channel channelSource)
            throws ProvisionFailedException {

        Optional<ObjectMask> maskAsObject = createObjectMask();

        try {
            return segment.segment(
                    channelSource,
                    maskAsObject,
                    createSeeds(channelSource.dimensions(), maskAsObject));
        } catch (SegmentationFailedException e) {
            throw new ProvisionFailedException(e);
        }
    }

    /**
     * Creates an {@link ObjectMask} from the optional mask provider.
     *
     * @return An {@link Optional} containing the created {@link ObjectMask}, or empty if no mask is provided.
     * @throws ProvisionFailedException if the mask creation fails.
     */
    private Optional<ObjectMask> createObjectMask() throws ProvisionFailedException {
        return OptionalProviderFactory.create(mask).map(Mask::binaryVoxels).map(ObjectMask::new);
    }

    /**
     * Creates seed objects for the segmentation.
     *
     * @param dimensions The dimensions of the channel being segmented.
     * @param maskAsObject An optional mask to restrict the seed objects.
     * @return An {@link Optional} containing the created seed {@link ObjectCollection}, or empty if no seeds are provided.
     * @throws ProvisionFailedException if the seed creation fails.
     */
    private Optional<ObjectCollection> createSeeds(
            Dimensions dimensions, Optional<ObjectMask> maskAsObject)
            throws ProvisionFailedException {
        return OptionalUtilities.map(
                OptionalProviderFactory.create(objectsSeeds),
                objects -> createSeeds(objects, maskAsObject, dimensions));
    }

    /**
     * Creates seed objects, optionally restricted by a mask.
     *
     * @param seeds The initial seed objects.
     * @param maskAsObject An optional mask to restrict the seed objects.
     * @param dimensions The dimensions of the channel being segmented.
     * @return The created seed {@link ObjectCollection}.
     * @throws ProvisionFailedException if the seed creation fails.
     */
    private static ObjectCollection createSeeds(
            ObjectCollection seeds, Optional<ObjectMask> maskAsObject, Dimensions dimensions)
            throws ProvisionFailedException {
        try {
            return OptionalUtilities.map(
                            maskAsObject,
                            object ->
                                    SeedsFactory.createSeedsWithMask(
                                            seeds, object, new Point3i(0, 0, 0), dimensions))
                    .orElseGet(() -> SeedsFactory.createSeedsWithoutMask(seeds));
        } catch (CreateException e) {
            throw new ProvisionFailedException(e);
        }
    }
}