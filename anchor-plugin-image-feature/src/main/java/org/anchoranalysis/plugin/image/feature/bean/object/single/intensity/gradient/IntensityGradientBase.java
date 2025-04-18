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

package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity.gradient;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.feature.calculate.part.CalculationPart;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.spatial.point.Point3d;

public abstract class IntensityGradientBase extends FeatureSingleObject {

    // START BEAN PROPERTIES
    @BeanField @NonNegative @Getter @Setter private int energyIndexX = -1;

    @BeanField @NonNegative @Getter @Setter private int energyIndexY = -1;

    @BeanField @OptionalBean @Getter @Setter private int energyIndexZ = -1;

    @BeanField @Getter @Setter private int subtractConstant = 0;
    // END BEAN PROPERTIES

    protected CalculationPart<List<Point3d>, FeatureInputSingleObject> gradientCalculation() {
        return new CalculateGradientFromChannels(
                energyIndexX, energyIndexY, energyIndexZ, subtractConstant);
    }
}
