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

package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme;

import ch.ethz.biol.cell.mpp.cfg.proposer.CfgProposerEmpty;
import java.util.Arrays;
import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.mpp.io.input.MPPInitParamsFactory;
import org.anchoranalysis.mpp.sgmn.bean.kernel.Kernel;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent.KernelInitialCfgNRG;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent.pixelized.KernelBirthPixelized;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent.pixelized.KernelDeathPixelized;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.proposer.KernelProposerOptionSingle;
import org.anchoranalysis.test.bean.BeanTestChecker;
import org.anchoranalysis.test.image.BoundIOContextFixture;

class KernelProposerFixture {

    /**
     * Creates a proposer with a Birth and Death kernel (equal probability) for a MarkProposer
     *
     * @param markProposer mark-proposer to use when making births
     * @param context
     * @return
     * @throws CreateException
     * @throws InitException
     */
    public static KernelProposer<CfgNRGPixelized> createBirthAndDeath(MarkProposer markProposer)
            throws CreateException, InitException {

        BoundIOContext context = BoundIOContextFixture.withSuppressedLogger();

        MPPInitParams initParams =
                MPPInitParamsFactory.create(context, Optional.empty(), Optional.empty());

        KernelProposer<CfgNRGPixelized> kernelProposer =
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

    private static KernelInitialCfgNRG createInitialKernel(MPPInitParams params, Logger logger) {
        return BeanTestChecker.checkAndInit(
                new KernelInitialCfgNRG(new CfgProposerEmpty()), params, logger);
    }

    private static KernelBirthPixelized createBirthKernel(
            MarkProposer markProposer, MPPInitParams initParams, Logger logger) {
        return BeanTestChecker.checkAndInit(
                new KernelBirthPixelized(markProposer), initParams, logger);
    }
}
