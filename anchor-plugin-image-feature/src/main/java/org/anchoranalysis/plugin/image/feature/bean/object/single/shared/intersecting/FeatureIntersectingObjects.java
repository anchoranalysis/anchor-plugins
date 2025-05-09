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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.feature.calculate.cache.part.ResolvedPart;
import org.anchoranalysis.feature.initialization.FeatureInitialization;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;

/** An abstract base class for features that calculate values based on intersecting objects. */
public abstract class FeatureIntersectingObjects extends FeatureSingleObject {

    // START BEAN PROPERTIES
    /** ID for the particular object-collection */
    @BeanField @Getter @Setter private String id = "";

    /** The value to return when there are no objects in the search collection. */
    @BeanField @Getter @Setter private double valueNoObjects = Double.NaN;

    // END BEAN PROPERTIES

    /** The collection of objects to search for intersections. */
    private ObjectCollection searchObjects;

    @Override
    protected void beforeCalc(FeatureInitialization initialization) throws InitializeException {
        super.beforeCalc(initialization);

        ImageInitialization image = new ImageInitialization(initialization.sharedObjectsRequired());
        try {
            this.searchObjects = image.objects().getException(id);
        } catch (NamedProviderGetException e) {
            throw new InitializeException(e.summarize());
        }
    }

    @Override
    public double calculate(FeatureCalculationInput<FeatureInputSingleObject> input)
            throws FeatureCalculationException {

        if (getSearchObjects().isEmpty()) {
            return getValueNoObjects();
        }

        return valueFor(
                input,
                input.resolver().search(new CalculateIntersectingObjects(id, searchObjects)));
    }

    /**
     * Calculates a value based on the input and the intersecting objects.
     *
     * @param input the input for feature calculation
     * @param intersecting the resolved part containing intersecting objects
     * @return the calculated feature value
     * @throws FeatureCalculationException if an error occurs during calculation
     */
    protected abstract double valueFor(
            FeatureCalculationInput<FeatureInputSingleObject> input,
            ResolvedPart<ObjectCollection, FeatureInputSingleObject> intersecting)
            throws FeatureCalculationException;

    /**
     * Gets the collection of objects to search for intersections.
     *
     * @return the {@link ObjectCollection} used for searching intersections
     */
    protected ObjectCollection getSearchObjects() {
        return searchObjects;
    }
}
