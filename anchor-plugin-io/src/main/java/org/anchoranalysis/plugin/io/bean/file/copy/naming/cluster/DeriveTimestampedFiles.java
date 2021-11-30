/*-
 * #%L
 * anchor-plugin-io
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
package org.anchoranalysis.plugin.io.bean.file.copy.naming.cluster;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.io.input.file.FileWithDirectoryInput;
import org.anchoranalysis.math.statistics.MeanScale;
import org.anchoranalysis.math.statistics.VarianceCalculatorDouble;
import org.anchoranalysis.plugin.io.bean.file.pattern.TimestampPattern;

/**
 * Derives a list of {@link TimestampedFile} from a list of {@link FileWithDirectoryInput}.
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
class DeriveTimestampedFiles {

    // START REQUIRED ARGUMENTS
    private final List<TimestampPattern> dateTimePatterns;
    // END REQUIRED ARGUMENTS

    /**
     * The mean-and-scale calculated from all {@code inputs}. This is null until after {@link
     * #derive} has been called.
     */
    private MeanScale scaler;

    /**
     * Derives the timestamps for the inputs.
     *
     * @param inputs the inputs to derive from.
     * @param offset the offset to assume the time-stamp belongs in.
     * @return a newly created list of timestamped files.
     * @throws OperationFailedException
     */
    public List<TimestampedFile> derive(List<FileWithDirectoryInput> inputs, ZoneOffset offset)
            throws OperationFailedException {
        VarianceCalculatorDouble varianceCalculator = new VarianceCalculatorDouble();

        try {
            List<TimestampedFile> extracted =
                    FunctionalList.mapToList(
                            inputs,
                            CreateException.class,
                            input ->
                                    new TimestampedFile(
                                            input.getFile(),
                                            varianceCalculator,
                                            dateTimePatterns,
                                            offset));
            scaler = deriveScaler(varianceCalculator);
            extracted.forEach(attributes -> attributes.normalize(scaler));
            return extracted;
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * The mean-and-scale calculated from all {@code inputs}.
     *
     * @return the scaler.
     * @throws OperationFailedException if {@link #derive(List,ZoneOffset)} has not yet been called.
     */
    public MeanScale getScaler() throws OperationFailedException {
        return Optional.ofNullable(scaler)
                .orElseThrow(() -> new OperationFailedException("No scaler is yet available"));
    }

    /** Calculates the mean and standard-deviation from a {@link VarianceCalculatorDouble}. */
    private static MeanScale deriveScaler(VarianceCalculatorDouble varianceCalculator) {
        double stdDev = Math.sqrt(varianceCalculator.variance());
        return new MeanScale(varianceCalculator.mean(), stdDev);
    }
}
