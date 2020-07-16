/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

/**
 * Creates a binary img-chnl where all pixels are 'on' within a certain x, y, z coordinate range
 *
 * <p>This effectively creates a mask that is a box
 */
public class BinaryChnlProviderInsideBox extends BinaryChnlProviderDimSource {

    // START BEAN PROPERTIES
    /** Minimum X coordinate inclusive */
    @BeanField @Getter @Setter private int minX = 0;

    /** Maximum X coordinate inclusive */
    @BeanField @Getter @Setter private int maxX = Integer.MAX_VALUE;

    /** Minimum Y coordinate inclusive */
    @BeanField @Getter @Setter private int minY = 0;

    /** Maximum Y coordinate inclusive */
    @BeanField @Getter @Setter private int maxY = Integer.MAX_VALUE;

    /** Minimum Z coordinate inclusive */
    @BeanField @Getter @Setter private int minZ = 0;

    /** Maximum Z coordinate inclusive */
    @BeanField @Getter @Setter private int maxZ = Integer.MAX_VALUE;
    // END BEAN PROPERTIES

    @Override
    protected Mask createFromSource(ImageDimensions dimSource) throws CreateException {
        Channel chnl =
                ChannelFactory.instance()
                        .createEmptyInitialised(dimSource, VoxelDataTypeUnsignedByte.INSTANCE);

        BoundingBox bbox = createBox(dimSource);

        return createBinaryChnl(bbox, chnl);
    }

    private static Mask createBinaryChnl(BoundingBox bbox, Channel chnl) {
        BinaryValues bv = BinaryValues.getDefault();
        chnl.getVoxelBox().any().setPixelsTo(bbox, bv.getOnInt());
        return new Mask(chnl, bv);
    }

    private BoundingBox createBox(ImageDimensions sd) {
        BoundingBox bbox =
                new BoundingBox(new Point3d(minX, minY, minZ), new Point3d(maxX, maxY, maxZ));

        // Make sure box is inside channel
        return bbox.clipTo(sd.getExtent());
    }
}
