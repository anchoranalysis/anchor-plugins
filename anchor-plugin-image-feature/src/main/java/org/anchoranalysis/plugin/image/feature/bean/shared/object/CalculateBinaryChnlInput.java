/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.shared.object;

import java.nio.ByteBuffer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInputNRG;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.IncorrectVoxelDataTypeException;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateBinaryChnlInput<T extends FeatureInputNRG>
        extends FeatureCalculation<FeatureInputSingleObject, T> {

    private final Mask chnl;

    @Override
    protected FeatureInputSingleObject execute(T input) throws FeatureCalcException {

        BinaryVoxelBox<ByteBuffer> bvb = binaryVoxelBox(chnl);

        return new FeatureInputSingleObject(new ObjectMask(bvb), input.getNrgStackOptional());
    }

    private static BinaryVoxelBox<ByteBuffer> binaryVoxelBox(Mask bic) throws FeatureCalcException {
        VoxelBox<ByteBuffer> vb;
        try {
            vb = bic.getChannel().getVoxelBox().asByte();
        } catch (IncorrectVoxelDataTypeException e1) {
            throw new FeatureCalcException(
                    "binaryImgChnlProvider returned incompatible data type", e1);
        }

        return new BinaryVoxelBoxByte(vb, bic.getBinaryValues());
    }
}
