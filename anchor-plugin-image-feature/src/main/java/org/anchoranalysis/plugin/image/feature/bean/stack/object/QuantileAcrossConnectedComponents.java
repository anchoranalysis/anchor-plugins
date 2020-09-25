/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.image.feature.bean.stack.object;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.ChildCacheName;
import org.anchoranalysis.feature.calculate.cache.ResolvedCalculation;
import org.anchoranalysis.feature.calculate.cache.SessionInput;
import org.anchoranalysis.image.feature.bean.stack.FeatureStack;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.object.ObjectCollection;

/** Calculates the median of a feature applied to each connected component */
public class QuantileAcrossConnectedComponents extends FeatureStack {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private Feature<FeatureInputSingleObject> item;

    @BeanField @Getter @Setter private int energyChannelIndex = 0;

    @BeanField @Getter @Setter private double quantile = 0.5;
    // END BEAN PROPERTIES

    @Override
    public double calculate(SessionInput<FeatureInputStack> input)
            throws FeatureCalculationException {

        ResolvedCalculation<ObjectCollection, FeatureInputStack> ccObjects =
                input.resolver().search(new CalculateConnectedComponents(energyChannelIndex));

        int size = input.calculate(ccObjects).size();

        DoubleArrayList featureVals = new DoubleArrayList();

        // Calculate a feature on each object-mask
        for (int i = 0; i < size; i++) {

            double val =
                    input.forChild()
                            .calculate(
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
                QuantileAcrossConnectedComponents.class, energyChannelIndex + "_" + index);
    }
}
