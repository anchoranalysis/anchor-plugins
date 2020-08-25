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

package org.anchoranalysis.plugin.mpp.bean.check;

import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.provider.Provider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.mpp.feature.error.CheckException;
import org.anchoranalysis.mpp.mark.Mark;

public abstract class CheckMarkWithMask extends CheckMark {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private Provider<Mask> mask;

    @BeanField @Getter @Setter private boolean acceptOutsideScene = false;
    // END BEAN PROPERTIES

    protected Mask createChannel() throws CheckException {
        try {
            return mask.create();
        } catch (CreateException e) {
            throw new CheckException(
                    String.format("Cannot create binary image (from provider %s)", mask), e);
        }
    }

    protected boolean isPointOnMask(
            Point3d cp, EnergyStack energyStack, Function<Point3d, Point3i> deriveFunc)
            throws CheckException {

        if (!energyStack.dimensions().contains(cp)) {
            return acceptOutsideScene;
        }

        return createChannel().isPointOn(deriveFunc.apply(cp));
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }
}
