/*-
 * #%L
 * anchor-plugin-mpp-feature
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

package org.anchoranalysis.plugin.mpp.feature.bean.mark.radii;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMark;
import org.anchoranalysis.anchor.mpp.mark.MarkConic;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.image.extent.ImageResolution;

public class RadiiRatio extends FeatureMark {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean highestTwoRadiiOnly = false;

    @BeanField @Getter @Setter private boolean suppressRes = false;
    // END BEAN PROPERTIES

    private ImageResolution uniformRes = new ImageResolution();

    @Override
    public double calc(SessionInput<FeatureInputMark> input) throws FeatureCalculationException {

        MarkConic markCast = (MarkConic) input.get().getMark();

        ImageResolution sr = suppressRes ? uniformRes : input.get().getResRequired();
        double[] radiiOrdered = markCast.radiiOrderedResolved(sr);

        int len = radiiOrdered.length;
        assert (len >= 2);

        if (len == 2) {
            return radiiOrdered[1] / radiiOrdered[0];
        } else {
            if (highestTwoRadiiOnly) {
                return radiiOrdered[2] / radiiOrdered[1];
            } else {
                return radiiOrdered[2] / radiiOrdered[0];
            }
        }
    }
}
