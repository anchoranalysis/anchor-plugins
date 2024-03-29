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

package org.anchoranalysis.plugin.image.task.bean.grouped.histogram;

import org.anchoranalysis.image.io.histogram.output.HistogramCSVGenerator;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.math.histogram.Histogram;

class GroupedHistogramWriter {

    /**
     * The output-name associated with <i>all</i> histograms written.
     *
     * <p>Note that this isn't actually used as part of the filenames outputted.
     */
    private final String outputName;

    private final HistogramCSVGenerator generator;

    public GroupedHistogramWriter(String outputName, boolean ignoreZeros) {
        this.outputName = outputName;
        this.generator = new HistogramCSVGenerator();
        this.generator.setIgnoreZeros(ignoreZeros);
    }

    public void writeHistogramToFile(
            Histogram histogram, String channelName, InputOutputContext context) {

        if (context.getOutputter().outputsEnabled().isOutputEnabled(outputName)) {
            context.getOutputter()
                    .writerSecondLevel(outputName)
                    .write(channelName, () -> generator, () -> histogram);
        }
    }
}
