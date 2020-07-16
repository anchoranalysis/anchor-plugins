/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.grouped.histogram;

import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.io.generator.histogram.HistogramCSVGenerator;
import org.anchoranalysis.io.output.bound.BoundIOContext;

class GroupedHistogramWriter {

    private final HistogramCSVGenerator generator;

    public GroupedHistogramWriter(boolean ignoreZeros) {
        generator = new HistogramCSVGenerator();
        generator.setIgnoreZeros(ignoreZeros);
    }

    public void writeHistogramToFile(Histogram hist, String outputName, BoundIOContext context) {
        generator.setIterableElement(hist);

        context.getOutputManager().getWriterCheckIfAllowed().write(outputName, () -> generator);
    }
}
