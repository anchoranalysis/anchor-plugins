/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjectsUnary;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.plugin.image.bean.object.segment.watershed.minima.MinimaImposition;

/**
 * Imposes minima in seed locations on the input-channel before performing the segmentation
 *
 * @author Owen Feehan
 */
public class ImposeMinima extends SegmentChannelIntoObjectsUnary {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MinimaImposition minimaImposition;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection segment(
            Channel chnl,
            Optional<ObjectMask> mask,
            Optional<SeedCollection> seeds,
            SegmentChannelIntoObjects upstreamSegmentation)
            throws SegmentationFailedException {

        if (!seeds.isPresent()) {
            throw new SegmentationFailedException("seeds must be present");
        }

        try {
            return upstreamSegmentation.segment(
                    chnlWithImposedMinima(chnl, seeds.get(), mask), mask, seeds);

        } catch (OperationFailedException e) {
            throw new SegmentationFailedException(e);
        }
    }

    private Channel chnlWithImposedMinima(
            Channel chnl, SeedCollection seeds, Optional<ObjectMask> object)
            throws OperationFailedException {
        if (!seeds.isEmpty()) {
            return minimaImposition.imposeMinima(chnl, seeds, object);
        } else {
            return chnl;
        }
    }
}
