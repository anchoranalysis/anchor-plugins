/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.segment;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromEntireChnlFactory;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithChannel;

public class SegmentChannel extends ObjectCollectionProviderWithChannel {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private BinaryChnlProvider mask;

    @BeanField @Getter @Setter private SegmentChannelIntoObjects sgmn;

    @BeanField @OptionalBean @Getter @Setter private ObjectCollectionProvider objectsSeeds;
    // END BEAN PROPERTIES

    @Override
    protected ObjectCollection createFromChnl(Channel chnlSource) throws CreateException {

        Optional<ObjectMask> maskAsObject = createMask();

        try {
            return sgmn.segment(
                    chnlSource,
                    maskAsObject,
                    createSeeds(chnlSource.getDimensions(), maskAsObject));
        } catch (SegmentationFailedException e) {
            throw new CreateException(e);
        }
    }

    private Optional<ObjectMask> createMask() throws CreateException {
        return OptionalFactory.create(mask).map(CreateFromEntireChnlFactory::createObject);
    }

    private Optional<SeedCollection> createSeeds(
            ImageDimensions dimensions, Optional<ObjectMask> maskAsObject) throws CreateException {
        return OptionalUtilities.map(
                OptionalFactory.create(objectsSeeds),
                objects -> createSeeds(objects, maskAsObject, dimensions));
    }

    private static SeedCollection createSeeds(
            ObjectCollection seeds, Optional<ObjectMask> maskAsObject, ImageDimensions dim)
            throws CreateException {
        return OptionalUtilities.map(
                        maskAsObject,
                        mask ->
                                SeedsFactory.createSeedsWithMask(
                                        seeds, mask, new Point3i(0, 0, 0), dim))
                .orElseGet(() -> SeedsFactory.createSeedsWithoutMask(seeds));
    }
}
