/* (C)2020 */
package org.anchoranalysis.plugin.ij.bean.object.segment;

import ij.process.ImageProcessor;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.seed.SeedCollection;

/**
 * Creates an object for each separate intensity-value (beginning at {@code startingColor}) by
 * flood-filling.
 *
 * <p>This algorithm only works with 2D images.
 *
 * @author Owen Feehan
 */
public class FloodFillEachIntensityLevel extends SegmentChannelIntoObjects {

    // START BEAN PROPERTIES
    /**
     * Only objects whose bounding-box volume is greater or equal to this threshold are included. By
     * default, all objects are included.
     */
    @BeanField @Getter @Setter private int minimumBoundingBoxVolume = 1;

    /**
     * The first intensity-value to consider as a valid object (e.g. usually 0 is ignored as
     * background)
     */
    @BeanField @Getter @Setter private int startingIntensity = 1;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection segment(
            Channel channel, Optional<ObjectMask> mask, Optional<SeedCollection> seeds)
            throws SegmentationFailedException {
        checkUnsupportedMask(mask);
        checkUnsupportedSeeds(seeds);
        checkUnsupported3D(channel);

        try {
            int numColors = floodFillChnl(channel);
            return objectsFromLabels(channel, numColors);

        } catch (OperationFailedException e) {
            throw new SegmentationFailedException(e);
        }
    }

    /**
     * Flood fills a channel, converting it into objects each labelled with an incrementing integer
     * id
     *
     * @param chnl channel to flood-fill
     * @return the number of objects (each corresponding to intensity level 1.... N)
     * @throws OperationFailedException
     */
    private int floodFillChnl(Channel chnl) throws OperationFailedException {
        BinaryValuesByte bv = BinaryValuesByte.getDefault();
        ImageProcessor imageProcessor =
                IJWrap.imageProcessorByte(chnl.getVoxelBox().asByte().getPlaneAccess(), 0);
        return new FloodFillHelper(minimumBoundingBoxVolume, bv.getOnByte(), imageProcessor)
                .floodFill2D(startingIntensity);
    }

    /**
     * Create object-masks from an image labelled as per {@link floodFillChnl}
     *
     * @param chnl a channel labelled as per {@link floodFillChnl}
     * @param numLabels the number of objects, so that the label ids are a sequence (1,numLabels)
     *     inclusive.
     * @return a derived collection of objects
     */
    private ObjectCollection objectsFromLabels(Channel chnl, int numLabels) {
        return CreateFromLabels.create(
                chnl.getVoxelBox().asByte(), 1, numLabels, minimumBoundingBoxVolume);
    }
}
