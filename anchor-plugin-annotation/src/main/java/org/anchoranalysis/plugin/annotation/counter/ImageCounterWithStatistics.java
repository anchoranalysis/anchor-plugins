package org.anchoranalysis.plugin.annotation.counter;

import org.anchoranalysis.annotation.io.comparer.StatisticsToExport;

/**
 * Like {@link ImageCounter} but also exports statistics.
 *
 * @author Owen Feehan
 */
public interface ImageCounterWithStatistics<T> extends ImageCounter<T> {

    /**
     * Creates statistics from counted images and the payload {@code T} passed for annotated images.
     *
     * @return the statistics.
     */
    public StatisticsToExport comparison();
}
