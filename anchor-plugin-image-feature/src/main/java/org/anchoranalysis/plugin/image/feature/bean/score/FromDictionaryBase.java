/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.score;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.math.histogram.Histogram;

/** An abstract base class for features that require a {@link Dictionary} for initialization. */
public abstract class FromDictionaryBase extends SingleChannel {

    /**
     * Initializes the feature with histograms and a dictionary.
     *
     * @param histograms a list of {@link Histogram}s
     * @param dictionary an optional {@link Dictionary}
     * @throws InitializeException if the dictionary is not present or initialization fails
     */
    @Override
    public void initialize(List<Histogram> histograms, Optional<Dictionary> dictionary)
            throws InitializeException {

        if (!dictionary.isPresent()) {
            throw new InitializeException(
                    "This pixel-score requires a dictionary to be present, but it is not.");
        }

        setupDictionary(dictionary.get());
    }

    /**
     * Sets up the feature using the provided dictionary.
     *
     * @param dictionary the {@link Dictionary} to use for setup
     * @throws InitializeException if setup fails
     */
    protected abstract void setupDictionary(Dictionary dictionary) throws InitializeException;

    /**
     * Extracts a double value from the dictionary for a given key.
     *
     * @param dictionary the {@link Dictionary} to extract from
     * @param key the key to look up in the dictionary
     * @return the double value associated with the key
     * @throws InitializeException if the key does not exist in the dictionary
     */
    protected static double extractAsDouble(Dictionary dictionary, String key)
            throws InitializeException {
        if (!dictionary.containsKey(key)) {
            throw new InitializeException(String.format("Key '%s' does not exist", key));
        }

        return dictionary.getAsDouble(key);
    }
}
