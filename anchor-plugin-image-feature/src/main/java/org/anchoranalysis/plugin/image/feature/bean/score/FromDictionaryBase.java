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

public abstract class FromDictionaryBase extends SingleChannel {

    @Override
    public void initialize(List<Histogram> histograms, Optional<Dictionary> dictionary)
            throws InitializeException {

        if (!dictionary.isPresent()) {
            throw new InitializeException(
                    "This pixel-score requires a dictionary to be present, but it is not.");
        }

        setupDictionary(dictionary.get());
    }

    protected abstract void setupDictionary(Dictionary dictionary) throws InitializeException;

    protected static double extractAsDouble(Dictionary dictionary, String key)
            throws InitializeException {
        if (!dictionary.containsKey(key)) {
            throw new InitializeException(String.format("Key '%s' does not exist", key));
        }

        return dictionary.getAsDouble(key);
    }
}
