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
/* (C)2020 */
package ch.ethz.biol.cell.mpp.pair.addcriteria;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.feature.addcriteria.AddCriteriaPair;
import org.anchoranalysis.anchor.mpp.feature.addcriteria.IncludeMarksFailureException;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.mark.MarkDistance;
import org.anchoranalysis.anchor.mpp.mark.UnsupportedMarkTypeException;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.extent.ImageDimensions;

public class AddCriteriaDistanceTo extends AddCriteriaPair {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private UnitValueDistance threshold;

    @BeanField @Getter @Setter private MarkDistance distance;
    // END BEAN PROPERTIES

    @Override
    public boolean includeMarks(
            VoxelizedMarkMemo mark1,
            VoxelizedMarkMemo mark2,
            ImageDimensions dimensions,
            Optional<FeatureCalculatorMulti<FeatureInputPairMemo>> session,
            boolean do3D)
            throws IncludeMarksFailureException {
        double d;
        try {
            d = distance.distance(mark1.getMark(), mark2.getMark());
        } catch (UnsupportedMarkTypeException e) {
            throw new IncludeMarksFailureException(e);
        }

        try {
            double thresholdVal =
                    threshold.resolve(
                            Optional.of(dimensions.getRes()),
                            mark1.getMark().centerPoint(),
                            mark2.getMark().centerPoint());
            return d < thresholdVal;

        } catch (OperationFailedException e) {
            throw new IncludeMarksFailureException(e);
        }
    }

    @Override
    public Optional<FeatureList<FeatureInputPairMemo>> orderedListOfFeatures() {
        return Optional.empty();
    }
}
