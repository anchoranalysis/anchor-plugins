/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.stack.object;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.factory.CreateFromConnectedComponentsFactory;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateConnectedComponents extends FeatureCalculation<ObjectCollection, FeatureInputStack> {

    private final int nrgChnlIndex;

    @Override
    protected ObjectCollection execute(FeatureInputStack input) throws FeatureCalcException {

        try {
            Mask binaryImgChnl =
                    new Mask(
                            input.getNrgStackRequired().getChnl(nrgChnlIndex),
                            BinaryValues.getDefault());

            CreateFromConnectedComponentsFactory creator =
                    new CreateFromConnectedComponentsFactory();
            return creator.createConnectedComponents(binaryImgChnl);

        } catch (CreateException e) {
            throw new FeatureCalcException(e);
        }
    }
}
