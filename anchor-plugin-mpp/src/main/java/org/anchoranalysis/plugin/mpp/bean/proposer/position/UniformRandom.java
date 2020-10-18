/*-
 * #%L
 * anchor-plugin-mpp
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

package org.anchoranalysis.plugin.mpp.bean.proposer.position;

import java.util.Optional;
import java.util.function.ToIntFunction;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.mpp.bean.proposer.PositionProposer;
import org.anchoranalysis.mpp.proposer.ProposerContext;
import org.anchoranalysis.spatial.point.Point3d;

public class UniformRandom extends PositionProposer {

    // START BEAN
    @BeanField @Getter @Setter private boolean suppressZ = false;
    // END BEAN

    @Override
    public Optional<Point3d> propose(ProposerContext context) {

        double x = sampleAlongAxis(context, Dimensions::x);
        double y = sampleAlongAxis(context, Dimensions::y);
        double z = suppressZ ? 0 : sampleAlongAxis(context, Dimensions::z);
        return Optional.of(new Point3d(x, y, z));
    }

    private static double sampleAlongAxis(
            ProposerContext context, ToIntFunction<Dimensions> extentForAxis) {
        return context.getRandomNumberGenerator()
                .sampleDoubleFromRange(extentForAxis.applyAsInt(context.dimensions()));
    }
}
