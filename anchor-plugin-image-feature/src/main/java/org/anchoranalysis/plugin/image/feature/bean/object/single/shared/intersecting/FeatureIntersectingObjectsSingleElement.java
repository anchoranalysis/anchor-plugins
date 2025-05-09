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

package org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.feature.calculate.cache.ChildCacheName;
import org.anchoranalysis.feature.calculate.cache.part.ResolvedPart;
import org.anchoranalysis.image.feature.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;

/**
 * A feature that calculates a value based on intersecting objects, considering each intersecting
 * object individually.
 */
public abstract class FeatureIntersectingObjectsSingleElement extends FeatureIntersectingObjects {

    // START BEAN PROPERTIES
    /** The feature to calculate for each intersecting object pair. */
    @BeanField @Getter @Setter private Feature<FeatureInputPairObjects> item;

    // END BEAN PROPERTIES

    @Override
    protected double valueFor(
            FeatureCalculationInput<FeatureInputSingleObject> input,
            ResolvedPart<ObjectCollection, FeatureInputSingleObject> intersecting)
            throws FeatureCalculationException {

        return aggregateResults(calculateResults(input, intersecting));
    }

    /**
     * Aggregates the results from individual intersecting object calculations.
     *
     * @param results the list of results from individual calculations
     * @return the aggregated result
     */
    protected abstract double aggregateResults(List<Double> results);

    /**
     * Calculates results for each intersecting object.
     *
     * @param inputExisting the existing input for feature calculation
     * @param ccIntersecting the resolved part containing intersecting objects
     * @return a list of calculated results
     * @throws FeatureCalculationException if an error occurs during calculation
     */
    private List<Double> calculateResults(
            FeatureCalculationInput<FeatureInputSingleObject> inputExisting,
            ResolvedPart<ObjectCollection, FeatureInputSingleObject> ccIntersecting)
            throws FeatureCalculationException {

        int size = inputExisting.calculate(ccIntersecting).size();

        List<Double> results = new ArrayList<>();
        for (int i = 0; i < size; i++) {

            final int index = i;

            double res =
                    inputExisting
                            .forChild()
                            .calculate(
                                    item,
                                    new CalculateIntersecting(ccIntersecting, index),
                                    new ChildCacheName(
                                            FeatureIntersectingObjectsSingleElement.class, i));
            results.add(res);
        }
        return results;
    }
}
