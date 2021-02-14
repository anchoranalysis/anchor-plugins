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

package org.anchoranalysis.plugin.image.task.bean.format;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.value.StringUtilities;

/**
 * Calculates an output-name based upon the number of series and timepoints.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
class CalculateOutputName {

    /** The index of the current series. */
    private int seriesIndex;

    /** The index of the current timepoint. */
    private int timeIndex;

    /** The total number of elements in the series to be outputted. */
    private int numberSeries;

    /** The total number of timepoints to be outputted. */
    private int numberTimepoints;

    /**
     * If true, the series index is not included in the outputted file-names.
     *
     * <p>It is always suppressed if only a single series exists.
     */
    private boolean suppressSeries;

    /**
     * Calculates an output-name.
     *
     * <p>The output-name is unique across {@code seriesIndex} and {@code timeIndex}, but drops
     * either variable if there is a single element only. So an empty-string is returned if it's
     * single series, single time-point.
     *
     * @param existingName any existing name that may exist.
     * @return a unique outputName
     */
    public Optional<String> calculateOutputName(Optional<String> existingName) {
        return StringUtilities.joinNonEmpty(
                "_", calculateSeriesComponent(), calculateTimeComponent(), existingName.orElse(""));
    }

    private String calculateSeriesComponent() {
        if (suppressSeries || numberSeries <= 1) {
            return "";
        } else {
            return String.format("%03d", seriesIndex);
        }
    }

    private String calculateTimeComponent() {
        if (numberTimepoints <= 1) {
            return "";
        } else {
            return String.format("%05d", timeIndex);
        }
    }
}
