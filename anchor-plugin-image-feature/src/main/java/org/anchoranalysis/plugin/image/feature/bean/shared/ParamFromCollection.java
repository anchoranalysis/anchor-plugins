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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.core.value.KeyValueParams;
import org.anchoranalysis.feature.bean.operator.FeatureOperator;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureInitParams;
import org.anchoranalysis.feature.calculate.cache.SessionInput;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;

/**
 * Retrieves a parameter from a collection in shared-objects.
 *
 * <p>This differs from {@link org.anchoranalysis.plugin.operator.feature.bean.Param} which reads
 * the parameter from the energy-stack, whereas this from a specific parameters collection.
 *
 * @author Owen Feehan
 * @param <T> feature-input-type
 */
public class ParamFromCollection<T extends FeatureInput> extends FeatureOperator<T> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String collectionID = "";

    @BeanField @Getter @Setter private String key = "";
    // END BEAN PROPERTIES

    private double val;

    @Override
    protected void beforeCalc(FeatureInitParams paramsInit) throws InitException {
        super.beforeCalc(paramsInit);

        ImageInitParams imageInit = new ImageInitParams(paramsInit.sharedObjectsRequired());
        try {
            KeyValueParams kpv =
                    imageInit
                            .params()
                            .getNamedKeyValueParamsCollection()
                            .getException(collectionID);
            this.val = kpv.getPropertyAsDouble(key);

        } catch (NamedProviderGetException e) {
            throw new InitException(e.summarize());
        }
    }

    @Override
    public double calculate(SessionInput<T> input) throws FeatureCalculationException {
        return val;
    }
}
