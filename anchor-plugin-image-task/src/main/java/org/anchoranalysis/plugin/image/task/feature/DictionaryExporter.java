/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.feature;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.feature.results.ResultsVector;
import org.anchoranalysis.io.generator.serialized.DictionaryGenerator;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.io.output.writer.ElementSupplier;
import org.anchoranalysis.plugin.image.task.bean.feature.source.FromHistogram;

/** Exports a ResultVector as a {@link Dictionary}. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class DictionaryExporter {

    private static final String MANIFEST_FUNCTION = "dictionary";

    public static void export(
            FeatureNameList featureNames,
            ResultsVector results,
            String outputName,
            InputOutputContext context) {
        writeDictionary(
                () -> convert(featureNames, results, context.getLogger()),
                context.getOutputter(),
                outputName);
    }

    private static void writeDictionary(
            ElementSupplier<Dictionary> dictionary, Outputter outputter, String outputName) {
        outputter
                .writerSelective()
                .write(outputName, () -> new DictionaryGenerator(MANIFEST_FUNCTION), dictionary);
    }

    private static Dictionary convert(
            FeatureNameList featureNames, ResultsVector rv, Logger logger) {
        assert (featureNames.size() == rv.size());

        Dictionary dictionary = new Dictionary();
        for (int i = 0; i < featureNames.size(); i++) {

            String key = featureNames.get(i);
            Optional<Double> val = rv.getResult(i);

            if (val.isPresent()) {
                dictionary.put(key, val.get());
            } else {
                // Then an error happened and we report it
                logger.errorReporter().recordError(FromHistogram.class, rv.getError(i));
                dictionary.put(key, Double.NaN);
            }
        }
        return dictionary;
    }
}
