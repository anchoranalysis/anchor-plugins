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

package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair;

import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.CalculateOverlap;
import lombok.Getter;
import lombok.Setter;
import java.util.function.Function;
import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeaturePairMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;

public abstract class FeaturePairMemoSingleRegion extends FeaturePairMemo {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
    // END BEAN PROPERTIES

    protected double overlappingNumVoxels(SessionInput<FeatureInputPairMemo> input)
            throws FeatureCalcException {
        return input.calc(new CalculateOverlap(regionID));
    }

    protected BoundingBox bbox(
            FeatureInputPairMemo input,
            Function<FeatureInputPairMemo, VoxelizedMarkMemo> funcExtract)
            throws FeatureCalcException {
        ImageDimensions sd = input.getDimensionsRequired();
        return funcExtract.apply(input).getMark().bbox(sd, getRegionID());
    }
}
