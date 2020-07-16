/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.filter.independent;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.axis.AxisType;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.plugin.image.bean.object.filter.ObjectFilterPredicate;

/**
 * Only keep objects where at least one voxel (on a particular channel) has intensity greater or
 * equal to a threshold.
 *
 * @author Owen Feehan
 */
public class IntensityGreaterEqualThan extends ObjectFilterPredicate {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChnlProvider chnl;

    // The threshold we use, the distance is always calculated in the direction of the XY plane.
    @BeanField @Getter @Setter private UnitValueDistance threshold;
    // END BEAN PROPERTIES

    private VoxelBox<?> vb;

    @Override
    protected void start(Optional<ImageDimensions> dim, ObjectCollection objectsToFilter)
            throws OperationFailedException {

        Channel chnlSingleRegion;
        try {
            chnlSingleRegion = chnl.create();
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
        assert (chnlSingleRegion != null);
        vb = chnlSingleRegion.getVoxelBox().any();
    }

    @Override
    protected boolean match(ObjectMask object, Optional<ImageDimensions> dim)
            throws OperationFailedException {

        int thresholdResolved = threshold(dim);

        for (int z = 0; z < object.getBoundingBox().extent().getZ(); z++) {

            ByteBuffer bb = object.getVoxelBox().getPixelsForPlane(z).buffer();

            int z1 = z + object.getBoundingBox().cornerMin().getZ();
            VoxelBuffer<?> bbChnl = vb.getPixelsForPlane(z1);

            for (int y = 0; y < object.getBoundingBox().extent().getY(); y++) {
                for (int x = 0; x < object.getBoundingBox().extent().getX(); x++) {

                    int offset = object.getBoundingBox().extent().offset(x, y);
                    if (bb.get(offset) == object.getBinaryValuesByte().getOnByte()) {

                        int y1 = y + object.getBoundingBox().cornerMin().getY();
                        int x1 = x + object.getBoundingBox().cornerMin().getX();

                        int offsetGlobal = vb.extent().offset(x1, y1);

                        // Now we get a value from the vb
                        int val = bbChnl.getInt(offsetGlobal);
                        if (val >= thresholdResolved) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private int threshold(Optional<ImageDimensions> dim) throws OperationFailedException {
        return (int)
                Math.ceil(threshold.resolveForAxis(dim.map(ImageDimensions::getRes), AxisType.X));
    }

    @Override
    protected void end() throws OperationFailedException {
        vb = null;
    }
}
