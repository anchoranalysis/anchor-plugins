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

package org.anchoranalysis.plugin.image.feature.bean.shared;

import java.util.Dictionary;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.core.identifier.provider.store.SharedObjects;
import org.anchoranalysis.feature.bean.operator.FeatureGeneric;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.feature.initialization.FeatureInitialization;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;

/**
 * Retrieves a parameter as stored in a {@link org.anchoranalysis.core.value.Dictionary} in {@link
 * org.anchoranalysis.core.identifier.provider.store.SharedObjects}.
 *
 * <p>This differs from {@link org.anchoranalysis.plugin.operator.feature.bean.FromDictionary} which
 * reads the parameter from the energy-stack, whereas this from a specific parameters collection.
 *
 * @author Owen Feehan
 * @param <T> feature-input-type
 */
public class ParamFromCollection<T extends FeatureInput> extends FeatureGeneric<T> {

    // START BEAN PROPERTIES
    /** The name of the {@link Dictionary} that will be retrieved from {@link SharedObjects}. */
    @BeanField @Getter @Setter private String dictionary = "";

    /** The name of the key in the dictionary, whose corresponding value will be returned. */
    @BeanField @Getter @Setter private String key = "";

    // END BEAN PROPERTIES

    private double value;

    @Override
    protected void beforeCalc(FeatureInitialization initialization) throws InitializeException {
        super.beforeCalc(initialization);

        ImageInitialization image = new ImageInitialization(initialization.sharedObjectsRequired());
        try {
            this.value = image.dictionaries().getException(dictionary).getAsDouble(key);

        } catch (NamedProviderGetException e) {
            throw new InitializeException(e.summarize());
        }
    }

    @Override
    public double calculate(FeatureCalculationInput<T> input) throws FeatureCalculationException {
        return value;
    }
}
