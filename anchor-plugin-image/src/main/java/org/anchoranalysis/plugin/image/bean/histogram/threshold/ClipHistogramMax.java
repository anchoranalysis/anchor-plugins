/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.histogram.threshold;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.GreaterThanBean;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToConstant;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevelOne;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramArray;

/**
 * Clips the input-histogram to a certain maximum value, and then delegates the calculate-level.
 *
 * @author Owen Feehan
 */
@EqualsAndHashCode(callSuper = true)
public class ClipHistogramMax extends CalculateLevelOne {

    // START BEAN
    @BeanField @Getter @Setter private int max;
    // END BEAN

    @Override
    public int calculateLevel(Histogram histogram) throws OperationFailedException {
        return calculateLevelIncoming(createClipped(histogram, max));
    }

    private static Histogram createClipped(Histogram histogram, int maxVal) {
        Preconditions.checkArgument(maxVal <= histogram.getMaxBin());

        long numAbove =
                histogram.countThreshold(new RelationToConstant(new GreaterThanBean(), maxVal));

        Histogram out = new HistogramArray(histogram.getMaxBin());
        for (int i = histogram.getMinBin(); i <= maxVal; i++) {
            out.incrValBy(i, histogram.getCount(i));
        }
        out.incrValBy(maxVal, numAbove);
        return out;
    }
}
