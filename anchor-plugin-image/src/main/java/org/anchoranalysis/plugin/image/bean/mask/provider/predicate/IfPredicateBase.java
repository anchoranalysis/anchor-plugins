/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.bean.mask.provider.predicate;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.bean.provider.MaskProviderUnary;
import org.anchoranalysis.image.core.mask.Mask;

/**
 * Base class which multiplexes between the current mask and an alternative depending if a condition
 * is met.
 *
 * <p>This class extends {@link MaskProviderUnary} to provide a mechanism for conditional mask
 * selection.
 */
public abstract class IfPredicateBase extends MaskProviderUnary {

    // START BEAN PROPERTIES
    /**
     * Returned instead of {@code mask} if the predicate is not satisfied.
     *
     * <p>This {@link MaskProvider} is used to generate an alternative mask when the predicate
     * condition is not met.
     */
    @BeanField @Getter @Setter private MaskProvider maskElse;
    // END BEAN PROPERTIES

    @Override
    protected Mask createFromMask(Mask mask) throws ProvisionFailedException {
        if (predicate(mask)) {
            return mask;
        } else {
            return maskElse.get();
        }
    }

    /**
     * Evaluates a predicate condition on the input mask.
     *
     * @param mask the input {@link Mask} to evaluate
     * @return true if the predicate condition is satisfied, false otherwise
     * @throws ProvisionFailedException if there's an error during predicate evaluation
     */
    protected abstract boolean predicate(Mask mask) throws ProvisionFailedException;
}
