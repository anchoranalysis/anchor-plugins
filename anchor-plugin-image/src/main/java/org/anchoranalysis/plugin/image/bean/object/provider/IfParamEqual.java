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

package org.anchoranalysis.plugin.image.bean.object.provider;

import lombok.Getter;
import lombok.Setter;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.dictionary.DictionaryProvider;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.voxel.object.ObjectCollection;

/**
 * Multiplexes between two object-collection-providers depending on whether a parameter value equals
 * a particular string
 *
 * <p>If the parameter value doesn't exist or is null, an exception is thrown.
 *
 * @author Owen Feehan
 */
public class IfParamEqual extends ObjectCollectionProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider whenEqual;

    @BeanField @Getter @Setter private ObjectCollectionProvider whenNotEqual;

    @BeanField @Getter @Setter private DictionaryProvider params;

    @BeanField @Getter @Setter private String key;

    @BeanField @Getter @Setter private String value;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection create() throws CreateException {

        Optional<String> dictionaryValue = params.create().getAsString(key);

        if (!dictionaryValue.isPresent()) {
            throw new CreateException(String.format("Dictionary key does not exist: %s", key));
        }

        if (dictionaryValue.get().equals(value)) {
            return whenEqual.create();
        } else {
            return whenNotEqual.create();
        }
    }
}
