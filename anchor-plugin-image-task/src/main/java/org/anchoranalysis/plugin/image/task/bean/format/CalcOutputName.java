/* (C)2020 */
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
