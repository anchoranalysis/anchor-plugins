/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.shared;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculatePairInput
        extends FeatureCalculation<FeatureInputPairObjects, FeatureInputSingleObject> {

    private final Mask chnl;

    @Override
    protected FeatureInputPairObjects execute(FeatureInputSingleObject input)
            throws FeatureCalcException {

        ObjectMask objFromBinary = new ObjectMask(chnl.binaryVoxelBox());

        return new FeatureInputPairObjects(
                input.getObject(), objFromBinary, input.getNrgStackOptional());
    }
}
