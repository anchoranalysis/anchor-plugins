/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.shared.object;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.CalcForChild;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.input.FeatureInputNRG;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.feature.bean.FeatureNRGStack;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * Calculates as object-masks from entities in shared, using the feature-input only for a nrg-stack.
 *
 * @author Owen Feehan
 * @param <T> feature-input
 */
public abstract class FeatureSingleObjectFromShared<T extends FeatureInputNRG>
        extends FeatureNRGStack<T> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private Feature<FeatureInputSingleObject> item;
    // END BEAN PROPERTIES

    @Override
    protected void beforeCalc(FeatureInitParams paramsInit) throws InitException {
        super.beforeCalc(paramsInit);
        beforeCalcWithImageInitParams(new ImageInitParams(paramsInit.sharedObjectsRequired()));
    }

    protected abstract void beforeCalcWithImageInitParams(ImageInitParams imageInit)
            throws InitException;

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalcException {
        return calc(input.forChild(), item);
    }

    protected abstract double calc(
            CalcForChild<T> calcForChild, Feature<FeatureInputSingleObject> featureForSingleObject)
            throws FeatureCalcException;

    @Override
    public Class<? extends FeatureInput> inputType() {
        return FeatureInputNRG.class;
    }
}
