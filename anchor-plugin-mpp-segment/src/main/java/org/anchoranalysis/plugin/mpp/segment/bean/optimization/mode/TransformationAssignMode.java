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

package org.anchoranalysis.plugin.mpp.segment.bean.optimization.mode;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.mpp.bean.anneal.AnnealScheme;
import org.anchoranalysis.mpp.segment.bean.optimization.ExtractScoreSize;
import org.anchoranalysis.mpp.segment.bean.optimization.StateReporter;
import org.anchoranalysis.plugin.mpp.segment.bean.optimization.kernelbridge.KernelStateBridge;
import org.anchoranalysis.plugin.mpp.segment.optimization.AccptProbCalculator;

/**
 * Applies a transformation to the kernel-type U to calculate the Energy used as the primary readout
 * during optimization
 *
 * <p>However, the kernel manipulation layer will always function in terms of the untransformed
 * Energy (U) as the optimization continues
 *
 * <p>The final transformation, as well as what's "reported" out use the TRANSFORMED (S) version
 *
 * @author Owen Feehan
 */
public class TransformationAssignMode<S, T, U> extends AssignMode<S, T, U> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private KernelStateBridge<U, T> kernelStateBridge;

    @BeanField @Getter @Setter private StateReporter<T, S> stateReporter;

    @BeanField @Getter @Setter private ExtractScoreSize<S> extractScoreSizeReport;

    @BeanField @Getter @Setter private ExtractScoreSize<T> extractScoreSizeState;
    // END BEAN PROPERTIES

    @Override
    public AccptProbCalculator<T> probCalculator(AnnealScheme annealScheme) {
        return new AccptProbCalculator<>(annealScheme, extractScoreSizeState);
    }

    @Override
    public KernelStateBridge<U, T> kernelStateBridge() {
        return kernelStateBridge;
    }

    @Override
    public StateReporter<T, S> stateReporter() {
        return stateReporter;
    }

    @Override
    public ExtractScoreSize<S> extractScoreSizeReport() {
        return extractScoreSizeReport;
    }

    @Override
    public ExtractScoreSize<T> extractScoreSizeState() {
        return extractScoreSizeState;
    }
}
