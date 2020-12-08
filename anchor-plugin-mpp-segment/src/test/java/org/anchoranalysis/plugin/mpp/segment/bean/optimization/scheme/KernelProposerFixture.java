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

package org.anchoranalysis.plugin.mpp.segment.bean.optimization.scheme;

import java.util.Arrays;
import java.util.Optional;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.io.InitParamsContext;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.io.input.MPPInitParamsFactory;
import org.anchoranalysis.mpp.segment.bean.kernel.Kernel;
import org.anchoranalysis.mpp.segment.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.plugin.mpp.bean.proposer.mark.collection.CreateNew;
import org.anchoranalysis.plugin.mpp.segment.bean.kernel.independent.KernelInitialMarksVoxelized;
import org.anchoranalysis.plugin.mpp.segment.bean.kernel.independent.pixelized.KernelBirthPixelized;
import org.anchoranalysis.plugin.mpp.segment.bean.kernel.independent.pixelized.KernelDeathPixelized;
import org.anchoranalysis.plugin.mpp.segment.bean.kernel.proposer.KernelProposerOptionSingle;
import org.anchoranalysis.test.experiment.BeanTestChecker;
import org.anchoranalysis.test.image.InputOutputContextFixture;

class KernelProposerFixture {

    /**
     * Creates a proposer with a Birth and Death kernel (equal probability) for a MarkProposer
     *
     * @param markProposer mark-proposer to use when making births
     * @return
     * @throws CreateException
     * @throws InitException
     */
    public static KernelProposer<VoxelizedMarksWithEnergy> createBirthAndDeath(
            MarkProposer markProposer) throws CreateException, InitException {

        InputOutputContext context = InputOutputContextFixture.withSuppressedLogger();

        MPPInitParams initParams =
                MPPInitParamsFactory.create( new InitParamsContext(context), Optional.empty(), Optional.empty());

        KernelProposer<VoxelizedMarksWithEnergy> kernelProposer =
                createProposerTwoEqual(
                        createInitialKernel(initParams, context.getLogger()),
                        createBirthKernel(markProposer, initParams, context.getLogger()),
                        new KernelDeathPixelized());
        kernelProposer.init();
        return BeanTestChecker.check(kernelProposer);
    }

    /** create a proposer with two equal options */
    private static <T> KernelProposer<T> createProposerTwoEqual(
            Kernel<T> initialKernel, Kernel<T> kernel1, Kernel<T> kernel2) {
        KernelProposer<T> proposer = new KernelProposer<>();
        proposer.setInitialKernel(initialKernel);
        proposer.setOptionList(
                Arrays.asList(
                        new KernelProposerOptionSingle<>(kernel1, 0.5),
                        new KernelProposerOptionSingle<>(kernel2, 0.5)));
        return proposer;
    }

    private static KernelInitialMarksVoxelized createInitialKernel(
            MPPInitParams params, Logger logger) {
        return BeanTestChecker.checkAndInit(
                new KernelInitialMarksVoxelized(new CreateNew()), params, logger);
    }

    private static KernelBirthPixelized createBirthKernel(
            MarkProposer markProposer, MPPInitParams initParams, Logger logger) {
        return BeanTestChecker.checkAndInit(
                new KernelBirthPixelized(markProposer), initParams, logger);
    }
}
