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

package org.anchoranalysis.plugin.image.bean.color.provider;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.image.bean.provider.ColorProvider;

/**
 * Combines one or more {@link ColorProvider}s into a unitary {@link ColorProvider}.
 *
 * <p>The combined unitary provider aggregates elements from all the child lists, preserving element
 * order, as they are added with {@link #setList(List)}.
 *
 * @author Owen Feehan
 */
public class Concatenate extends ColorProvider {

    // START BEAN PROPERTIES
    /** A list of {@link ColorProvider} to be concatenated. */
    @BeanField @Getter @Setter private List<ColorProvider> list = Arrays.asList();

    // END BEAN PROPERTIES

    @Override
    public ColorList get() throws ProvisionFailedException {

        ColorList out = new ColorList();

        for (ColorProvider provider : list) {
            out.addAll(provider.get());
        }

        return out;
    }
}
