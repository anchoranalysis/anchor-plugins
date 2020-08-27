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
package org.anchoranalysis.plugin.image.provider;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Creates reference (i.e. Providers that reference an existing object) of different types
 *
 * <p>This is particularly useful to avoid to referring to multiple identically named {@code
 * Reference} classes in the same file, necessitating the use of the fully qualified class name.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReferenceFactory {

    public static org.anchoranalysis.plugin.image.bean.stack.provider.Reference stack(String id) {
        return new org.anchoranalysis.plugin.image.bean.stack.provider.Reference(id);
    }

    public static org.anchoranalysis.plugin.image.bean.channel.provider.Reference channel(
            String id) {
        return new org.anchoranalysis.plugin.image.bean.channel.provider.Reference(id);
    }

    public static org.anchoranalysis.plugin.image.bean.object.provider.Reference objects(
            String id) {
        return new org.anchoranalysis.plugin.image.bean.object.provider.Reference(id);
    }

    public static org.anchoranalysis.plugin.image.bean.mask.provider.Reference mask(String id) {
        return new org.anchoranalysis.plugin.image.bean.mask.provider.Reference(id);
    }

    public static org.anchoranalysis.plugin.image.bean.histogram.provider.Reference histogram(
            String id) {
        return new org.anchoranalysis.plugin.image.bean.histogram.provider.Reference(id);
    }
}
