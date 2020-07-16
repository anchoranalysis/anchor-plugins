/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.nrg;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.IncorrectVoxelDataTypeException;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernel3NeighborMatchValue;

/**
 * Calculates the number of voxels on the object that have a neighbor (according to a binary-mask on
 * an nrg-channel)
 *
 * <p>The nrg-channel should be a binary-channel (with 255 high, and 0 low) showing all possible
 * neighbor voxels
 *
 * @author Owen Feehan
 */
public class NumberNeighboringVoxels extends SpecificNRGChannelBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean outsideAtThreshold = false;

    @BeanField @Getter @Setter private boolean ignoreAtThreshold = false;

    @BeanField @Getter @Setter private boolean do3D = false;
    // END BEAN PROPERTIES

    @Override
    protected double calcWithChannel(ObjectMask object, Channel chnl) throws FeatureCalcException {

        OutlineKernel3NeighborMatchValue kernelMatch =
                new OutlineKernel3NeighborMatchValue(
                        outsideAtThreshold, do3D, object, binaryVoxelBox(chnl), ignoreAtThreshold);
        return ApplyKernel.applyForCount(kernelMatch, object.getVoxelBox());
    }

    private BinaryVoxelBox<ByteBuffer> binaryVoxelBox(Channel chnl) throws FeatureCalcException {
        try {
            VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();
            return new BinaryVoxelBoxByte(vb, BinaryValues.getDefault());

        } catch (IncorrectVoxelDataTypeException e) {
            throw new FeatureCalcException(
                    String.format("nrgStack channel %d has incorrect data type", getNrgIndex()), e);
        }
    }
}
