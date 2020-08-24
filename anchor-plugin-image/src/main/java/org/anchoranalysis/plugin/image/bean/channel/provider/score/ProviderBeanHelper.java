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

package org.anchoranalysis.plugin.image.bean.channel.provider.score;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.provider.Provider;
import org.anchoranalysis.core.error.CreateException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ProviderBeanHelper {

    /**
     * Creates a list of ProviderType from Beans
     *
     * @param in input-list
     * @return a newly-created list containing the newly created items
     * @throws CreateException if a provider fails to create
     */
    public static <T> List<T> listFromBeans(List<? extends Provider<T>> in)
            throws CreateException {
        List<T> out = new ArrayList<>();
        addFromBeanList(in, out);
        return out;
    }

    /**
     * Creates items from a list of providers, and adds them to an output-list
     *
     * @param in input-list
     * @param out output-list
     * @throws CreateException if a provider fails to create
     */
    private static <T> void addFromBeanList(List<? extends Provider<T>> in, List<T> out)
            throws CreateException {
        for (Provider<T> provider : in) {
            out.add(provider.create());
        }
    }
}
