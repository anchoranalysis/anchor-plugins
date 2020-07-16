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

import ch.ethz.biol.cell.mpp.anneal.AnnealSchemeGeom;
import ch.ethz.biol.cell.mpp.feedback.reporter.ConsoleAggReporter;
import ch.ethz.biol.cell.mpp.feedback.reporter.NullReporter;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.mark.factory.MarkFactory;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.feature.nrg.scheme.NRGSchemeWithSharedFeatures;
import org.anchoranalysis.bean.init.params.NullInitParams;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.random.RandomNumberGeneratorMersenne;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.OptScheme;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.termination.TriggerTerminationCondition;
import org.anchoranalysis.mpp.sgmn.optscheme.DualStack;
import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;
import org.anchoranalysis.mpp.sgmn.optscheme.OptSchemeContext;
import org.anchoranalysis.mpp.sgmn.optscheme.OptTerminatedEarlyException;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.mode.DirectAssignMode;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.termination.NumberIterations;
import org.anchoranalysis.test.bean.BeanTestChecker;
import org.anchoranalysis.test.image.BoundIOContextFixture;

class OptSchemeFixture {

    private OptSchemeFixture() {}

    /** A simulated annealing scheme using direct-assign mode */
    public static <T> OptScheme<T, T> simulatedAnnealing(
            ExtractScoreSize<T> extractScoreSize, int numberOfIterations) {
        OptSchemeSimulatedAnnealing<T, T, T> optScheme = new OptSchemeSimulatedAnnealing<>();
        optScheme.setAnnealScheme(new AnnealSchemeGeom(1e-20, 0.99995));
        optScheme.setAssignMode(new DirectAssignMode<>(extractScoreSize));
        optScheme.setTermCondition(new NumberIterations(numberOfIterations));
        return BeanTestChecker.check(optScheme);
    }

    /**
     * Finds the optimimum
     *
     * @param optScheme optimization-scheme
     * @param kernelProposer kernel-proposer
     * @param markFactory creates a new mark when a "birth" occurs
     * @param nrgScheme how the NRG is calculated
     * @param nrgStack the stack on which features are applied to calcualte the NRG
     * @param logToConsole iff TRUE prints ongoing optimziation progress to console (useful for
     *     debugging)
     * @return the optimal state according to the algorithm
     * @throws OptTerminatedEarlyException
     * @throws CreateException
     */
    public static CfgNRGPixelized findOpt(
            OptScheme<CfgNRGPixelized, CfgNRGPixelized> optScheme,
            KernelProposer<CfgNRGPixelized> kernelProposer,
            MarkFactory markFactory,
            NRGSchemeWithSharedFeatures nrgScheme,
            NRGStackWithParams nrgStack,
            boolean logToConsole)
            throws OptTerminatedEarlyException, CreateException {

        return optScheme.findOpt(
                kernelProposer,
                new ListUpdatableMarkSetCollection(),
                logToConsole ? new ConsoleAggReporter(2) : new NullReporter<>(),
                createContext(
                        markFactory,
                        nrgScheme,
                        nrgStack,
                        BoundIOContextFixture.withSuppressedLogger()));
    }

    private static OptSchemeContext createContext(
            MarkFactory markFactory,
            NRGSchemeWithSharedFeatures nrgScheme,
            NRGStackWithParams nrgStack,
            BoundIOContext context)
            throws CreateException {

        CfgGen cfgGen = new CfgGen(markFactory);
        BeanTestChecker.checkAndInit(cfgGen, NullInitParams.instance(), context.getLogger());

        // Uses a fixed random-seed so tests always generate the same result
        return new OptSchemeContext(
                "testExperiment",
                nrgScheme,
                new DualStack(nrgStack),
                new TriggerTerminationCondition(),
                context,
                new RandomNumberGeneratorMersenne(true),
                cfgGen);
    }
}
