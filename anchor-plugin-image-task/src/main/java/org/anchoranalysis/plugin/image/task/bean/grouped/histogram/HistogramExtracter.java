/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.grouped.histogram;

import lombok.AllArgsConstructor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;

/** Extracts a histogram from an image for a given key */
@AllArgsConstructor
class HistogramExtracter {

    private final ChannelSource source;
    private final String keyMask;
    private final int maskValue;

    public Histogram extractFrom(Channel chnl) throws JobExecutionException {

        try {
            if (!keyMask.isEmpty()) {
                Mask mask = extractMask(keyMask);
                return HistogramFactory.create(chnl, mask);
            } else {
                return HistogramFactory.create(chnl);
            }

        } catch (CreateException e) {
            throw new JobExecutionException("Cannot create histogram", e);
        }
    }

    private Mask extractMask(String stackName) throws JobExecutionException {
        try {
            Channel chnl = source.extractChnl(stackName, false);
            return new Mask(chnl, createMaskBinaryValues());

        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    private BinaryValues createMaskBinaryValues() throws JobExecutionException {
        if (maskValue == 255) {
            return new BinaryValues(0, 255);
        } else if (maskValue == 0) {
            return new BinaryValues(255, 0);
        } else {
            throw new JobExecutionException("Only mask-values of 255 or 0 are current supported");
        }
    }
}
