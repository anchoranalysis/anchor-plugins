/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.stack.object;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.stack.FeatureStack;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.object.ObjectCollection;

/** Calculates the median of a feature applied to each connected component */
public class QuantileAcrossConnectedComponents extends FeatureStack {

    // START BEAN PROPERTIES
    @BeanField private Feature<FeatureInputSingleObject> item;

    @BeanField private int nrgChnlIndex = 0;

    @BeanField private double quantile = 0.5;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputStack> input) throws FeatureCalcException {

        ResolvedCalculation<ObjectCollection, FeatureInputStack> ccObjects =
                input.resolver().search(new CalculateConnectedComponents(nrgChnlIndex));

        int size = input.calc(ccObjects).size();

        DoubleArrayList featureVals = new DoubleArrayList();

        // Calculate a feature on each obj mask
        for (int i = 0; i < size; i++) {

            double val =
                    input.forChild()
                            .calc(
                                    item,
                                    new CalculateDeriveObjFromCollection(ccObjects, i),
                                    cacheName(i));
            featureVals.add(val);
        }

        featureVals.sort();

        return Descriptive.quantile(featureVals, 0.5);
    }

    private ChildCacheName cacheName(int index) {
        return new ChildCacheName(
                QuantileAcrossConnectedComponents.class, nrgChnlIndex + "_" + index);
    }

    public Feature<FeatureInputSingleObject> getItem() {
        return item;
    }

    public void setItem(Feature<FeatureInputSingleObject> item) {
        this.item = item;
    }

    public int getNrgChnlIndex() {
        return nrgChnlIndex;
    }

    public void setNrgChnlIndex(int nrgChnlIndex) {
        this.nrgChnlIndex = nrgChnlIndex;
    }

    public double getQuantile() {
        return quantile;
    }

    public void setQuantile(double quantile) {
        this.quantile = quantile;
    }
}
