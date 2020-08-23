/*-
 * #%L
 * anchor-plugin-mpp-sgmn
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

package org.anchoranalysis.plugin.mpp.sgmn.bean.marks;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.points.CreateMarkFromPoints;
import org.anchoranalysis.anchor.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkCollection;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformerBean;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.optscheme.VoxelizedMarksWithEnergyFactory;

public class PointsFitter extends StateTransformerBean<MarkCollection, VoxelizedMarksWithEnergy> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private CreateMarkFromPoints createMark;
    // END BEAN PROPERTIES

    @Override
    public VoxelizedMarksWithEnergy transform(MarkCollection in, TransformationContext context)
            throws OperationFailedException {

        Optional<Mark> mark = createMark.fitMarkToPointsFromCfg(in, context.dimensions());

        // If we cannot create a mark, there is no proposal
        MarkCollection cfg = wrapMark(mark);

        try {
            return VoxelizedMarksWithEnergyFactory.createFromMarks(
                    cfg, context.getKernelCalcContext(), context.getLogger());
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private static MarkCollection wrapMark(Optional<Mark> mark) {
        MarkCollection marks = new MarkCollection();
        mark.ifPresent(marks::add);
        return marks;
    }
}
