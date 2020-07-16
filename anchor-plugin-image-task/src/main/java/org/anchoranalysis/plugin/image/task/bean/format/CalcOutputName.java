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

import java.util.ArrayList;
import java.util.List;

class CalcOutputName {

    private int seriesIndex;
    private int numSeries;

    private int t;
    private int sizeT;

    private boolean suppressSeries;

    public CalcOutputName(
            int seriesIndex, int numSeries, int t, int sizeT, boolean suppressSeries) {
        super();
        this.seriesIndex = seriesIndex;
        this.numSeries = numSeries;
        this.t = t;
        this.sizeT = sizeT;
        this.suppressSeries = suppressSeries;
    }

    public String calcOutputName(String existingName) {
        String outputName = maybePrependTimeSeries(existingName);

        // Catch the case where a string is empty (a single stack, with numSeries=1 and sizeT=1) and
        // give it a constant name
        if (!outputName.isEmpty()) {
            return outputName;
        } else {
            return "converted";
        }
    }

    private String maybePrependTimeSeries(String existingName) {

        List<String> components = new ArrayList<>();
        addToListIfNonEmpty(calculateSeriesComponent(), components);
        addToListIfNonEmpty(calculateTimeComponent(), components);
        addToListIfNonEmpty(existingName, components);
        return String.join("_", components);
    }

    private static void addToListIfNonEmpty(String str, List<String> list) {
        if (!str.isEmpty()) {
            list.add(str);
        }
    }

    private String calculateSeriesComponent() {
        if (suppressSeries || numSeries <= 1) {
            return "";
        } else {
            return String.format("%03d", seriesIndex);
        }
    }

    private String calculateTimeComponent() {
        if (sizeT <= 1) {
            return "";
        } else {
            return String.format("%05d", t);
        }
    }
}
