/*-
 * #%L
 * anchor-plugin-points
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
package org.anchoranalysis.plugin.points.calculate.ellipsoid;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.Operation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * Binds params with a CachedCalculation and exposes it as the {@link
 * org.anchoranalysis.core.functional.Operation} interface
 *
 * <p>(reverse-currying)
 *
 * @author Owen Feehan
 * @param <S> result-type
 * @param <T> params-type
 */
class CachedCalculationOperation<S, T extends FeatureInput>
        implements Operation<S, CreateException> {

    private ResolvedCalculation<S, T> cachedCalculation;
    private T params;

    public CachedCalculationOperation(ResolvedCalculation<S, T> cachedCalculation, T params) {
        super();
        this.cachedCalculation = cachedCalculation;
        this.params = params;
    }

    @Override
    public S doOperation() throws CreateException {
        try {
            return cachedCalculation.getOrCalculate(params);
        } catch (FeatureCalcException e) {
            throw new CreateException(e);
        }
    }
}
