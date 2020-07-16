/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.stack.object;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.object.ObjectMask;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CalculateDeriveObjectInput
        extends FeatureCalculation<FeatureInputSingleObject, FeatureInputStack> {

    private final int nrgIndex;

    @Override
    protected FeatureInputSingleObject execute(FeatureInputStack input)
            throws FeatureCalcException {
        return new FeatureInputSingleObject(extractObjectMask(input), input.getNrgStackOptional());
    }

    private ObjectMask extractObjectMask(FeatureInputStack input) throws FeatureCalcException {

        NRGStackWithParams nrgStack = input.getNrgStackRequired();

        Mask binary = new Mask(nrgStack.getChnl(nrgIndex), BinaryValues.getDefault());

        return new ObjectMask(binary.binaryVoxelBox());
    }
}
