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
package org.anchoranalysis.plugin.image.feature.bean.morphological;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonNegative;

/**
 * Specifies a certain number of iterations of the morphological operations of dilation and erosion
 *
 * @author Owen Feehan
 */
@EqualsAndHashCode(callSuper = false)
public class MorphologicalIterations extends AnchorBean<MorphologicalIterations> {

    // START BEAN PROPERTIES
    /** Number of times to perform morphological dilation */
    @BeanField @NonNegative @Getter @Setter private int iterationsDilation = 0;

    /** Number of times to perform morphological erosion */
    @BeanField @NonNegative @Getter @Setter private int iterationsErosion = 0;

    /** Whether to perform the morphological dimensions in 3D or 2D */
    @BeanField @Getter @Setter private boolean do3D = true;
    // END BEAN PROPERTIES

    /**
     * Checks that at least one of {@code iterationsDilation} and {@code iterationsErosion} is more
     * than 0
     */
    public boolean isAtLeastOnePositive() {
        return iterationsDilation > 0 || iterationsErosion > 0;
    }

    /** A string that uniquely identifies all properties in the bean, but friendly to humans */
    public String describePropertiesFriendly() {
        return String.format(
                "iterationsDilation=%d,iterationsErosion=%d,do3D=%s",
                iterationsDilation, iterationsErosion, do3D ? "true" : "false");
    }

    /** A string that uniquely identifies all properties in the bean, but NOT friendly to humans */
    public String uniquelyIdentifyAllProperties() {
        return iterationsDilation + "_" + iterationsErosion + "_" + do3D;
    }

    /** Copies the bean setting an new value for {@code iterationsDilation} */
    public MorphologicalIterations copyChangeIterationsDilation(int iterationsDilationsToAssign) {
        MorphologicalIterations duplicated = duplicateBean();
        duplicated.setIterationsDilation(iterationsDilationsToAssign);
        return duplicated;
    }
}
