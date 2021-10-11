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
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.io.InitializationContext;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.feature.mark.UpdatableMarksList;
import org.anchoranalysis.mpp.init.MarksInitialization;
import org.anchoranalysis.mpp.io.input.MarksInitializationFactory;
import org.anchoranalysis.mpp.segment.bean.optimization.kernel.Kernel;
import org.anchoranalysis.mpp.segment.bean.optimization.kernel.KernelProposer;
import org.anchoranalysis.plugin.mpp.bean.proposer.mark.collection.CreateNew;
import org.anchoranalysis.plugin.mpp.segment.bean.kernel.independent.KernelInitialMarksVoxelized;
import org.anchoranalysis.plugin.mpp.segment.bean.kernel.independent.voxelized.KernelBirthVoxelized;
import org.anchoranalysis.plugin.mpp.segment.bean.kernel.independent.voxelized.KernelDeathVoxelized;
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
     * @throws InitializeException
     */
    public static KernelProposer<VoxelizedMarksWithEnergy, UpdatableMarksList> createBirthAndDeath(
            MarkProposer markProposer) throws CreateException, InitializeException {

        InputOutputContext context = InputOutputContextFixture.withSuppressedLogger();

        MarksInitialization initialization =
                MarksInitializationFactory.create(
                        Optional.empty(), new InitializationContext(context), Optional.empty());

        KernelProposer<VoxelizedMarksWithEnergy, UpdatableMarksList> kernelProposer =
                createProposerTwoEqual(
                        createInitialKernel(initialization, context.getLogger()),
                        createBirthKernel(markProposer, initialization, context.getLogger()),
                        new KernelDeathVoxelized());
        kernelProposer.initialize();
        return BeanTestChecker.check(kernelProposer);
    }

    /** create a proposer with two equal options */
    private static <T, S> KernelProposer<T, S> createProposerTwoEqual(
            Kernel<T, S> initialKernel, Kernel<T, S> kernel1, Kernel<T, S> kernel2) {
        KernelProposer<T, S> proposer = new KernelProposer<>();
        proposer.setInitialKernel(initialKernel);
        proposer.setOptionList(
                Arrays.asList(
                        new KernelProposerOptionSingle<>(kernel1, 0.5),
                        new KernelProposerOptionSingle<>(kernel2, 0.5)));
        return proposer;
    }

    private static KernelInitialMarksVoxelized createInitialKernel(
            MarksInitialization params, Logger logger) {
        return BeanTestChecker.checkAndInit(
                new KernelInitialMarksVoxelized(new CreateNew()), params, logger);
    }

    private static KernelBirthVoxelized createBirthKernel(
            MarkProposer markProposer, MarksInitialization initialization, Logger logger) {
        return BeanTestChecker.checkAndInit(
                new KernelBirthVoxelized(markProposer), initialization, logger);
    }
}
