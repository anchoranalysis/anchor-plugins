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

package org.anchoranalysis.plugin.mpp.segment.bean.optimization.kernelbridge;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.segment.transformer.StateTransformer;
import org.anchoranalysis.mpp.segment.transformer.StateTransformerBean;
import org.anchoranalysis.plugin.mpp.segment.bean.marks.voxelized.RetrieveSourceFromVoxelized;
import org.anchoranalysis.plugin.mpp.segment.bean.marks.voxelized.VoxelizeWithTransform;
import org.anchoranalysis.plugin.mpp.segment.bean.optimization.mode.TransformMapOptional;
import org.anchoranalysis.plugin.mpp.segment.optimization.ToPixelized;

/**
 * State derived from the kernel takes the form of {@code ToPixelized<T>}
 *
 * @author Owen Feehan
 * @param <T>
 */
public class KernelStateBridgePixelize<T> extends KernelStateBridge<T, ToPixelized<T>> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private StateTransformerBean<T, VoxelizedMarksWithEnergy> transformer;
    // END BEAN PROPERTIES

    @Override
    public StateTransformer<Optional<T>, Optional<ToPixelized<T>>> kernelToState() {
        return new TransformMapOptional<>(new VoxelizeWithTransform<>(transformer));
    }

    @Override
    public StateTransformer<Optional<ToPixelized<T>>, Optional<T>> stateToKernel() {
        return new TransformMapOptional<>(new RetrieveSourceFromVoxelized<>());
    }
}