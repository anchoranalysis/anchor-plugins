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

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.bean.nonbean.segment.SegmentationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.core.object.MatchedObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.match.MatcherIntersectionHelper;
import org.anchoranalysis.plugin.image.bean.object.provider.WithChannelBase;
import org.anchoranalysis.spatial.box.BoundingBox;

/**
 * Takes each object one-by-one from {@code objectsSource}, and finds matching seeds from {@code
 * objectsSeeds}
 *
 * @author Owen Feehan
 */
public class SegmentWithSeeds extends WithChannelBase {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private ObjectCollectionProvider objectsSource;

    @BeanField @Getter @Setter private ObjectCollectionProvider objectsSeeds;

    @BeanField @Getter @Setter private SegmentChannelIntoObjects segment;

    // END BEAN PROPERTIES

    @Override
    protected ObjectCollection createFromChannel(Channel channel) throws ProvisionFailedException {

        ObjectCollection seeds = objectsSeeds.get();

        if (objectsSource != null) {
            ObjectCollection sourceObjects = objectsSource.get();
            return createWithSourceObjects(channel, seeds, sourceObjects, segment);
        } else {
            return createWithoutSourceObjects(channel, seeds, segment);
        }
    }

    private static ObjectCollection createWithSourceObjects(
            Channel channel,
            ObjectCollection seeds,
            ObjectCollection sourceObjects,
            SegmentChannelIntoObjects segment)
            throws ProvisionFailedException {

        assert (seeds != null);
        assert (sourceObjects != null);

        List<MatchedObject> matchList =
                MatcherIntersectionHelper.matchIntersectingObjects(sourceObjects, seeds);

        return ObjectCollectionFactory.flatMapFrom(
                matchList.stream(),
                CreateException.class,
                ows -> segmentIfMoreThanOne(ows, channel, segment));
    }

    private static ObjectCollection segmentIfMoreThanOne(
            MatchedObject ows, Channel channel, SegmentChannelIntoObjects segment)
            throws ProvisionFailedException {
        if (ows.numberMatches() <= 1) {
            return ObjectCollectionFactory.of(ows.getSource());
        } else {
            try {
                return segment(ows, channel, segment);
            } catch (SegmentationFailedException | CreateException e) {
                throw new ProvisionFailedException(e);
            }
        }
    }

    private static ObjectCollection createWithoutSourceObjects(
            Channel channel, ObjectCollection seedsAsObjects, SegmentChannelIntoObjects segment)
            throws ProvisionFailedException {

        try {
            return segment.segment(
                    channel,
                    Optional.empty(),
                    Optional.of(SeedsFactory.createSeedsWithoutMask(seedsAsObjects)));
        } catch (SegmentationFailedException e) {
            throw new ProvisionFailedException(e);
        }
    }

    // NB Objects in seeds are changed
    private static ObjectCollection segment(
            MatchedObject matchedObject, Channel channel, SegmentChannelIntoObjects sgmn)
            throws SegmentationFailedException, CreateException {

        BoundingBox boxSource = matchedObject.getSource().boundingBox();

        // We create a new object-mask for the new channel
        ObjectMask objectLocal = new ObjectMask(matchedObject.getSource().binaryVoxels());

        ObjectCollection seedsObj =
                SeedsFactory.createSeedsWithMask(
                        matchedObject.getMatches(),
                        objectLocal,
                        boxSource.cornerMin(),
                        channel.dimensions());

        ObjectCollection sgmnObjects =
                sgmn.segment(
                        createChannelForBox(channel, boxSource),
                        Optional.of(objectLocal),
                        Optional.of(seedsObj));

        // We shift each object back to were it belongs globally
        return sgmnObjects.shiftBy(boxSource.cornerMin());
    }

    private static Channel createChannelForBox(Channel channel, BoundingBox boundingBox) {
        // We make a channel just for the object
        return ChannelFactory.instance()
                .create(channel.extract().region(boundingBox, false), channel.resolution());
    }
}
