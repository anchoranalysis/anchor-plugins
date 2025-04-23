/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
import lombok.AllArgsConstructor;
import lombok.Value;
import org.anchoranalysis.feature.io.csv.FeatureCSVWriter;
import org.anchoranalysis.feature.io.csv.metadata.FeatureCSVMetadata;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;

/** Context for exporting features, containing configuration and output settings. */
@Value
@AllArgsConstructor
public class FeatureExporterContext {

    /** Context for reading from or writing outputs to the file-system. */
    private final InputOutputContext context;

    /**
     * When true, columns containing all {@link Double#NaN} values are removed before outputting.
     */
    private final boolean removeNaNColumns;

    /**
     * When true {@code double} values are printed to be as short as possible without losing
     * precision.
     */
    private final boolean visuallyShortenDecimals;

    /**
     * If false, an image is reported as errored, if any exception is thrown during calculation. If
     * true, then a value of {@link Double#NaN} is returned, and a message is written to the
     * error-log.
     */
    private final boolean suppressErrors;

    /**
     * Creates a {@link FeatureCSVWriter} for the non-aggregated results.
     *
     * @param metadata the metadata for the CSV file
     * @return an optional {@link FeatureCSVWriter} if creation is successful, empty otherwise
     * @throws OutputWriteFailedException if there's an error creating the CSV writer
     */
    public Optional<FeatureCSVWriter> csvWriter(FeatureCSVMetadata metadata)
            throws OutputWriteFailedException {
        return FeatureCSVWriter.create(metadata, context.getOutputter(), visuallyShortenDecimals);
    }
}
