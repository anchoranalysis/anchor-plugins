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

package org.anchoranalysis.plugin.image.bean.dictionary;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.dictionary.DictionaryProvider;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.image.bean.ImageBean;

/**
 * Checks if a value in a {@link Dictionary} is equal to an expected value.
 *
 * @author Owen Feehan
 */
public class DictionaryCondition extends ImageBean<DictionaryCondition> {

    // START BEAN PROPERTIES
    /** The dictionary to provide a value to check. */
    @BeanField @Getter @Setter private DictionaryProvider dictionary;

    /** The key in the dictionary whose value is checked. */
    @BeanField @Getter @Setter private String key = "";

    /**
     * The value the key should have in the dictionary, in order for the condition to be fulfilled.
     */
    @BeanField @Getter @Setter private String value = "";

    // END BEAN PROPERTIES

    /**
     * Checks if the condition is true by comparing the value in the dictionary with the expected
     * value.
     *
     * @return true if the value in the dictionary matches the expected value, false otherwise.
     * @throws ProvisionFailedException if the dictionary cannot be created or the key doesn't exist
     *     in the dictionary.
     */
    public boolean isConditionTrue() throws ProvisionFailedException {
        Dictionary dictionaryCreated = dictionary.get();
        String valueToMatch =
                dictionaryCreated
                        .getAsString(key)
                        .orElseThrow(
                                () ->
                                        new ProvisionFailedException(
                                                "No dictionary value exists for: " + key));
        return value.equals(valueToMatch);
    }
}
