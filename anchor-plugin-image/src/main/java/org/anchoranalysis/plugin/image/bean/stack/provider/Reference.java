/*-
 * #%L
 * anchor-image-bean
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

package org.anchoranalysis.plugin.image.bean.stack.provider;

import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.core.stack.Stack;

/**
 * Retrieves an existing stack.
 *
 * <p>The following types of objects are searched for a matching id, in this order:
 *
 * <ul>
 *   <li>stacks
 *   <li>channels
 *   <li>masks
 * </ul>
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class Reference extends StackProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String id = "";
    // END BEAN PROPERTIES

    private Stack stack;

    public Reference(String id) {
        this.id = id;
    }

    @Override
    public Stack create() throws CreateException {
        if (stack == null) {
            this.stack =
                    findMatchingStack()
                            .orElseThrow(
                                    () ->
                                            new CreateException(
                                                    "Cannot find a stack with id: " + id));
        }
        return stack;
    }

    private Optional<Stack> findMatchingStack() throws CreateException {
        try {
            return OptionalUtilities.orElseGetFlat(
                    OptionalUtilities.orElseGetFlat(fromStacks(), this::fromChannels),
                    this::fromMasks);
        } catch (NamedProviderGetException e) {
            throw new CreateException(e);
        }
    }

    private Optional<Stack> fromStacks() throws NamedProviderGetException {
        return getInitialization().stacks().getOptional(id);
    }

    private Optional<Stack> fromChannels() throws NamedProviderGetException {
        return getInitialization().channels().getOptional(id).map(Stack::new);
    }

    private Optional<Stack> fromMasks() throws NamedProviderGetException {
        return getInitialization()
                .masks()
                .getOptional(id)
                .map(mask -> new Stack(mask.channel()));
    }
}
