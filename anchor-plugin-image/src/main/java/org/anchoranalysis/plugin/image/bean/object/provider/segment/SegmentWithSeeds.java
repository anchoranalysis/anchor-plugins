/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.segment;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.plugin.image.bean.object.match.MatcherIntersectionHelper;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithChannel;

/**
 * Takes each object one-by-one from {@code objectsSource}, and finds matching seeds from {@code
 * objectsSeeds}
 *
 * @author Owen Feehan
 */
public class SegmentWithSeeds extends ObjectCollectionProviderWithChannel {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private ObjectCollectionProvider objectsSource;

    @BeanField @Getter @Setter private ObjectCollectionProvider objectsSeeds;

    @BeanField @Getter @Setter private SegmentChannelIntoObjects sgmn;
    // END BEAN PROPERTIES

    @Override
    protected ObjectCollection createFromChnl(Channel channel) throws CreateException {

        ObjectCollection seeds = objectsSeeds.create();

        if (objectsSource != null) {
            ObjectCollection sourceObjects = objectsSource.create();
            return createWithSourceObjects(channel, seeds, sourceObjects, sgmn);
        } else {
            return createWithoutSourceObjects(channel, seeds, sgmn);
        }
    }

    private static ObjectCollection createWithSourceObjects(
            Channel chnl,
            ObjectCollection seeds,
            ObjectCollection sourceObjects,
            SegmentChannelIntoObjects segment)
            throws CreateException {

        assert (seeds != null);
        assert (sourceObjects != null);

        List<MatchedObject> matchList =
                MatcherIntersectionHelper.matchIntersectingObjects(sourceObjects, seeds);

        return ObjectCollectionFactory.flatMapFrom(
                matchList.stream(),
                CreateException.class,
                ows -> segmentIfMoreThanOne(ows, chnl, segment));
    }

    private static ObjectCollection segmentIfMoreThanOne(
            MatchedObject ows, Channel channel, SegmentChannelIntoObjects segment)
            throws CreateException {
        if (ows.numMatches() <= 1) {
            return ObjectCollectionFactory.from(ows.getSource());
        } else {
            try {
                return sgmn(ows, channel, segment);
            } catch (SegmentationFailedException e) {
                throw new CreateException(e);
            }
        }
    }

    private static ObjectCollection createWithoutSourceObjects(
            Channel chnl, ObjectCollection seedsAsObjects, SegmentChannelIntoObjects sgmn)
            throws CreateException {

        try {
            return sgmn.segment(
                    chnl,
                    Optional.empty(),
                    Optional.of(SeedsFactory.createSeedsWithoutMask(seedsAsObjects)));
        } catch (SegmentationFailedException e) {
            throw new CreateException(e);
        }
    }

    // NB Objects in seeds are changed
    private static ObjectCollection sgmn(
            MatchedObject matchedObject, Channel channel, SegmentChannelIntoObjects sgmn)
            throws SegmentationFailedException, CreateException {

        BoundingBox bboxSource = matchedObject.getSource().getBoundingBox();

        // We create a new object-mask for the new channel
        ObjectMask objectLocal = new ObjectMask(matchedObject.getSource().binaryVoxelBox());

        SeedCollection seedsObj =
                SeedsFactory.createSeedsWithMask(
                        matchedObject.getMatches(),
                        objectLocal,
                        bboxSource.cornerMin(),
                        channel.getDimensions());

        ObjectCollection sgmnObjects =
                sgmn.segment(
                        createChannelForBox(channel, bboxSource),
                        Optional.of(objectLocal),
                        Optional.of(seedsObj));

        // We shift each object back to were it belongs globally
        return sgmnObjects.shiftBy(bboxSource.cornerMin());
    }

    private static Channel createChannelForBox(Channel channel, BoundingBox boundingBox) {
        // We make a channel just for the object
        return ChannelFactory.instance()
                .create(
                        channel.getVoxelBox().any().region(boundingBox, false),
                        channel.getDimensions().getRes());
    }
}
