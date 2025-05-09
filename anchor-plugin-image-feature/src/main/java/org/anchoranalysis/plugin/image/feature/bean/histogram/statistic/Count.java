/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.histogram.statistic;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToThreshold;
import org.anchoranalysis.image.feature.bean.histogram.FeatureHistogramStatistic;
import org.anchoranalysis.math.histogram.Histogram;

/**
 * Counts the number of items in a histogram (posssibly in relation to a threshold: above, below
 * etc.)
 *
 * @author Owen Feehan
 */
public class Count extends FeatureHistogramStatistic {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private RelationToThreshold threshold;

    // END BEAN PROPERTIES

    @Override
    protected double calculateStatisticFrom(Histogram histogram) {
        if (threshold != null) {
            return histogram.countMatching(threshold.asPredicateInt());
        } else {
            return histogram.getTotalCount();
        }
    }

    @Override
    public String describeParameters() {
        return String.format(
                "%f,%s,threshold=%s", threshold, super.describeParameters(), threshold.toString());
    }
}
