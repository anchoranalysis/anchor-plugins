/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;

public abstract class FeatureIntersectingObjectsSingleElement extends FeatureIntersectingObjects {

    // START BEAN PROPERTIES
    @BeanField private Feature<FeatureInputPairObjects> item;
    // END BEAN PROPERTIES

    @Override
    protected double valueFor(
            SessionInput<FeatureInputSingleObject> params,
            ResolvedCalculation<ObjectCollection, FeatureInputSingleObject> intersecting)
            throws FeatureCalcException {

        return aggregateResults(calcResults(params, intersecting));
    }

    protected abstract double aggregateResults(List<Double> results);

    private List<Double> calcResults(
            SessionInput<FeatureInputSingleObject> paramsExst,
            ResolvedCalculation<ObjectCollection, FeatureInputSingleObject> ccIntersecting)
            throws FeatureCalcException {

        int size = paramsExst.calc(ccIntersecting).size();

        List<Double> results = new ArrayList<>();
        for (int i = 0; i < size; i++) {

            final int index = i;

            double res =
                    paramsExst
                            .forChild()
                            .calc(
                                    item,
                                    new CalculateIntersecting(ccIntersecting, index),
                                    new ChildCacheName(
                                            FeatureIntersectingObjectsSingleElement.class, i));
            results.add(res);
        }
        return results;
    }

    public Feature<FeatureInputPairObjects> getItem() {
        return item;
    }

    public void setItem(Feature<FeatureInputPairObjects> item) {
        this.item = item;
    }
}
