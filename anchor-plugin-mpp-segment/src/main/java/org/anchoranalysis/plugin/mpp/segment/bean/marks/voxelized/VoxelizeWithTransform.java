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

package org.anchoranalysis.plugin.mpp.segment.bean.marks.voxelized;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.segment.transformer.StateTransformerBean;
import org.anchoranalysis.mpp.segment.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.segment.optimization.ToPixelized;

/**
 * Converts a Marks to a MarksToPixelized using a transformer
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
@AllArgsConstructor
public class VoxelizeWithTransform<T> extends StateTransformerBean<T, ToPixelized<T>> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private StateTransformerBean<T, VoxelizedMarksWithEnergy> transformer;
    // END BEAN PROPERTIES

    @Override
    public ToPixelized<T> transform(T in, TransformationContext context)
            throws OperationFailedException {
        return new ToPixelized<>(in, transformer.transform(in, context));
    }
}
