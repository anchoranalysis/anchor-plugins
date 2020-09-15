/*-
 * #%L
 * anchor-plugin-mpp
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
package org.anchoranalysis.plugin.mpp.bean.define;

import lombok.RequiredArgsConstructor;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.provider.ProviderAsStack;
import org.anchoranalysis.plugin.image.bean.stack.provider.color.ColoredBase;
import org.anchoranalysis.plugin.image.bean.stack.provider.color.ColoredMask;
import org.anchoranalysis.plugin.image.bean.stack.provider.color.ColoredObjects;
import org.anchoranalysis.plugin.image.provider.ReferenceFactory;
import org.anchoranalysis.plugin.mpp.bean.provider.collection.Reference;
import org.anchoranalysis.plugin.mpp.bean.stack.provider.ColoredMarks;

@RequiredArgsConstructor
class CreateVisualizatonHelper {

    private final String backgroundID;

    private final int outlineWidth;

    /** If true, backgroundID refers to a Stack, otherwise it's a Channel */
    private final boolean stackBackground;

    public StackProvider mask(String maskProviderID) {
        ColoredMask provider = new ColoredMask();
        provider.setMask(ReferenceFactory.mask(maskProviderID));
        configureProvider(provider);
        return provider;
    }

    public StackProvider objects(String objectsProviderID) {
        ColoredObjects provider = new ColoredObjects();
        provider.setObjects(ReferenceFactory.objects(objectsProviderID));
        configureProvider(provider);
        return provider;
    }

    public StackProvider marks(String marksProviderID) {
        ColoredMarks provider = new ColoredMarks();
        provider.setMarks(new Reference(marksProviderID));
        configureProvider(provider);
        return provider;
    }

    private void configureProvider(ColoredBase provider) {
        provider.setOutlineWidth(outlineWidth);
        provider.setBackground(backgroundStack());
    }

    private ProviderAsStack backgroundStack() {
        if (stackBackground) {
            return ReferenceFactory.stack(backgroundID);
        } else {
            return ReferenceFactory.channel(backgroundID);
        }
    }
}
