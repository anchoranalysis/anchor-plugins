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

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.energy.EnergyStack;

public class AndList extends CheckMark {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private List<CheckMark> list = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public boolean check(Mark mark, RegionMap regionMap, EnergyStack energyStack)
            throws CheckException {

        for (CheckMark item : list) {
            if (!item.check(mark, regionMap, energyStack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        for (CheckMark item : list) {
            if (!item.isCompatibleWith(testMark)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void start(EnergyStack energyStack) throws OperationFailedException {
        super.start(energyStack);
        for (CheckMark item : list) {
            item.start(energyStack);
        }
    }

    @Override
    public void end() {
        super.end();
        for (CheckMark item : list) {
            item.end();
        }
    }
}
