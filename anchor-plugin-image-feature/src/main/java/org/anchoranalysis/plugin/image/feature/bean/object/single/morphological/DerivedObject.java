/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.morphological;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.delegate.CalculateInputFromDelegateOption;

public abstract class DerivedObject extends FeatureSingleObject {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double emptyValue = 255;

    @BeanField @Getter @Setter private Feature<FeatureInputSingleObject> item;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

        ChildCacheName cacheName = cacheName();

        return CalculateInputFromDelegateOption.calc(
                input,
                createCachedCalculationForDerived(input.resolver()),
                CalculateObjForDerived::new,
                item,
                cacheName,
                emptyValue);
    }

    protected abstract FeatureCalculation<ObjectMask, FeatureInputSingleObject>
            createCachedCalculationForDerived(CalculationResolver<FeatureInputSingleObject> session)
                    throws FeatureCalcException;

    protected abstract ChildCacheName cacheName();
}
