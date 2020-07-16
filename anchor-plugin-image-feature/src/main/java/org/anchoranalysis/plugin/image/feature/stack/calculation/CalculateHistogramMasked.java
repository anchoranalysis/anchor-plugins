/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.stack.calculation;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;

/**
 * Calculated a histogram for a specific region on a channel, as identified by a mask in another
 * channel
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CalculateHistogramMasked extends FeatureCalculation<Histogram, FeatureInputStack> {

    /** the index in the nrg-stack of the channel part of whose signal will form a histogram */
    private final int nrgIndexSignal;

    /** the index in the nrg-stack of a channel which is a binary mask (0=off, 255=on) */
    private final int nrgIndexMask;

    @Override
    protected Histogram execute(FeatureInputStack input) throws FeatureCalcException {

        try {
            NRGStack nrgStack = input.getNrgStackRequired().getNrgStack();

            return HistogramFactory.create(extractChnl(nrgStack), extractMask(nrgStack));

        } catch (CreateException e) {
            throw new FeatureCalcException(e);
        }
    }

    private Channel extractChnl(NRGStack nrgStack) {
        return nrgStack.getChnl(nrgIndexSignal);
    }

    private Mask extractMask(NRGStack nrgStack) {
        Channel chnl = nrgStack.getChnl(nrgIndexMask);
        return new Mask(chnl, BinaryValues.getDefault());
    }
}
