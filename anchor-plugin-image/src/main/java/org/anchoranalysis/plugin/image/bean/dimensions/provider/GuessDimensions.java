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

package org.anchoranalysis.plugin.image.bean.dimensions.provider;

import java.util.Set;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.core.identifier.provider.store.NamedProviderStore;
import org.anchoranalysis.image.bean.provider.DimensionsProvider;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.stack.StackIdentifiers;

/**
 * Guesses dimensions from an <i>input-image</i> if it exists.
 *
 * <p>Otherwise, throws an exception indicating they should be set explicitly.
 *
 * <p>The <i>input-image</i> is a stack with the identifier {@link StackIdentifiers#INPUT_IMAGE}.
 *
 * @author Owen Feehan
 */
public class GuessDimensions extends DimensionsProvider {

    private Dimensions dimensions;

    @Override
    public Dimensions get() throws ProvisionFailedException {

        if (dimensions == null) {
            try {
                dimensions = takeDimensionFromStackCollection(getInitialization().stacks());
            } catch (InitializeException e) {
                throw new ProvisionFailedException(e);
            }
        }

        return dimensions;
    }

    private Dimensions takeDimensionFromStackCollection(NamedProviderStore<Stack> stackCollection)
            throws ProvisionFailedException {

        Set<String> keys = stackCollection.keys();

        if (!keys.contains(StackIdentifiers.INPUT_IMAGE)) {
            throw new ProvisionFailedException(
                    String.format(
                            "No input-image (%s) exists so cannot guess image dimensions. Please set the dimensions explicitly.",
                            StackIdentifiers.INPUT_IMAGE));
        }

        return dimensionsFromSpecificStack(StackIdentifiers.INPUT_IMAGE);
    }

    /** Takes the ImageDim from a particular stack in the collection */
    private Dimensions dimensionsFromSpecificStack(String keyThatExists)
            throws ProvisionFailedException {
        Stack stack;
        try {
            stack = getInitialization().stacks().getException(keyThatExists);
        } catch (NamedProviderGetException | InitializeException e) {
            throw new ProvisionFailedException(e);
        }

        Channel channel = stack.getChannel(0);
        if (channel == null) {
            throw new ProvisionFailedException(
                    String.format(
                            "Stack %s has no channels, so dimensions cannot be inferred.",
                            keyThatExists));
        }
        return channel.dimensions();
    }
}
