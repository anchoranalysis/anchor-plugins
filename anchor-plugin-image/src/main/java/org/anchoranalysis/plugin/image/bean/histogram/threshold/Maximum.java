/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.bean.histogram.threshold;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.math.histogram.Histogram;

/**
 * The maximum value of one or more {#link org.anchoranalysis.image.bean.threshold.CalculateLevel}
 *
 * @author Owen Feehan
 */
@EqualsAndHashCode(callSuper = true)
public class Maximum extends CalculateLevelListBase {

    @Override
    public int calculateLevel(Histogram h) throws OperationFailedException {

        int max = -1; // As level should always be >=0
        for (CalculateLevel cl : getList()) {
            int level = cl.calculateLevel(h);
            if (level > max) {
                max = level;
            }
        }
        return max;
    }
}
