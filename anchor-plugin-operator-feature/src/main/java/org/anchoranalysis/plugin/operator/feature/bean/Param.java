/*-
 * #%L
 * anchor-plugin-operator-feature
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
/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.bean.operator.FeatureOperator;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.input.FeatureInputParams;

/**
 * Extracts a key-value-param as a double
 *
 * <p>This differs from {@link
 * org.anchoranalysis.plugin.image.feature.bean.stack.ParamFromCollection} which reads the parameter
 * from a collection in the shared-objects, rather than from the nrg-stack.
 *
 * <p>Note the key has an optional prefix and suffix, so that the actual key used is <code>
 * ${keyPrefix}${key}${keySuffix}</code>
 *
 * @author Owen Feehan
 * @param <T> feature-input type
 */
public class Param<T extends FeatureInputParams> extends FeatureOperator<T> {

    // START BEAN PROPERTIES
    /** Prefix prepended to key */
    @BeanField @AllowEmpty @Getter @Setter String keyPrefix = "";

    @BeanField @AllowEmpty @Getter @Setter private String key = "";

    @BeanField @AllowEmpty @Getter @Setter String keySuffix = "";
    // END BEAN PROPERTIES

    private String keyAggregated;

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        if (keyPrefix.isEmpty() && key.isEmpty() && keySuffix.isEmpty()) {
            throw new BeanMisconfiguredException(
                    "At least one of keyPrefix, key and keySuffix must be non-empty");
        }
    }

    @Override
    protected void beforeCalc(FeatureInitParams paramsInit) throws InitException {
        super.beforeCalc(paramsInit);
        keyAggregated = keyAggregated();
    }

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalcException {

        KeyValueParams kvp = input.get().getParamsRequired();

        if (kvp.containsKey(keyAggregated)) {
            return kvp.getPropertyAsDouble(keyAggregated);
        } else {
            throw new FeatureCalcException(String.format("Param '%s' is missing", keyAggregated));
        }
    }

    private String keyAggregated() {
        return keyPrefix + key + keySuffix;
    }
}
