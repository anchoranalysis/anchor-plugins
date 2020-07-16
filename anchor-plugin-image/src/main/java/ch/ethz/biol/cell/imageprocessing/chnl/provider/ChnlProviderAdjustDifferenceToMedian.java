/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;

// Corrects a channel in the following way
//  For each object:
//		1. Identify the median value from channelLookup
//		2. Calculate the difference of each pixel value in channelLookup to Value 1.
//		3. Adjust each pixel value by Value 2.
public class ChnlProviderAdjustDifferenceToMedian extends ChnlProviderOneObjectsSource {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChnlProvider chnlLookup;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromChnl(Channel chnl, ObjectCollection objectsSource)
            throws CreateException {

        Channel lookup = DimChecker.createSameSize(chnlLookup, "chnlLookup", chnl);

        try {
            for (ObjectMask object : objectsSource) {

                Histogram histogram =
                        HistogramFactory.create(lookup.getVoxelBox(), Optional.of(object));
                adjustObject(object, chnl, lookup, (int) Math.round(histogram.mean()));
            }

            return chnl;

        } catch (OperationFailedException e) {
            throw new CreateException("An error occurred calculating the mean", e);
        }
    }

    private void adjustObject(
            ObjectMask object, Channel chnl, Channel chnlLookup, int medianFromObject) {

        ReadableTuple3i cornerMin = object.getBoundingBox().cornerMin();
        ReadableTuple3i cornerMax = object.getBoundingBox().calcCornerMax();

        VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();
        VoxelBox<ByteBuffer> vbLookup = chnlLookup.getVoxelBox().asByte();

        for (int z = cornerMin.getZ(); z <= cornerMax.getZ(); z++) {

            ByteBuffer bbChnl = vb.getPixelsForPlane(z).buffer();
            ByteBuffer bbChnlLookup = vbLookup.getPixelsForPlane(z).buffer();
            ByteBuffer bbMask =
                    object.getVoxelBox().getPixelsForPlane(z - cornerMin.getZ()).buffer();

            int maskOffset = 0;
            for (int y = cornerMin.getY(); y <= cornerMax.getY(); y++) {
                for (int x = cornerMin.getX(); x <= cornerMax.getX(); x++) {

                    if (bbMask.get(maskOffset++) == object.getBinaryValuesByte().getOnByte()) {

                        int offset = vb.extent().offset(x, y);

                        int lookupVal = ByteConverter.unsignedByteToInt(bbChnlLookup.get(offset));
                        int adj = (medianFromObject - lookupVal);

                        int crntVal = ByteConverter.unsignedByteToInt(bbChnl.get(offset));
                        int valNew = crntVal - adj;

                        if (valNew < 0) valNew = 0;
                        if (valNew > 255) valNew = 255;

                        bbChnl.put(offset, (byte) valNew);
                    }
                }
            }
        }
    }
}
