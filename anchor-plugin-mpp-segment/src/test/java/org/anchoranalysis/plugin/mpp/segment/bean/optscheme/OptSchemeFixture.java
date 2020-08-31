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

package org.anchoranalysis.plugin.mpp.segment.bean.optscheme;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.initializable.params.NullInitParams;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.random.RandomNumberGeneratorMersenne;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.mpp.bean.mark.MarkWithIdentifierFactory;
import org.anchoranalysis.mpp.bean.mark.factory.MarkFactory;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.feature.energy.scheme.EnergySchemeWithSharedFeatures;
import org.anchoranalysis.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.mpp.segment.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.segment.bean.optscheme.OptScheme;
import org.anchoranalysis.mpp.segment.bean.optscheme.termination.TriggerTerminationCondition;
import org.anchoranalysis.mpp.segment.optscheme.DualStack;
import org.anchoranalysis.mpp.segment.optscheme.ExtractScoreSize;
import org.anchoranalysis.mpp.segment.optscheme.OptSchemeContext;
import org.anchoranalysis.mpp.segment.optscheme.OptTerminatedEarlyException;
import org.anchoranalysis.plugin.mpp.segment.bean.annealing.Geometry;
import org.anchoranalysis.plugin.mpp.segment.bean.optscheme.mode.DirectAssignMode;
import org.anchoranalysis.plugin.mpp.segment.bean.optscheme.termination.NumberIterations;
import org.anchoranalysis.plugin.mpp.segment.optscheme.reporter.ConsoleAggregatedReporter;
import org.anchoranalysis.plugin.mpp.segment.optscheme.reporter.NullReporter;
import org.anchoranalysis.test.experiment.BeanTestChecker;
import org.anchoranalysis.test.image.BoundIOContextFixture;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class OptSchemeFixture {

    /** A simulated annealing scheme using direct-assign mode */
    public static <T> OptScheme<T, T> simulatedAnnealing(
            ExtractScoreSize<T> extractScoreSize, int numberOfIterations) {
        OptSchemeSimulatedAnnealing<T, T, T> optScheme = new OptSchemeSimulatedAnnealing<>();
        optScheme.setAnnealScheme(new Geometry(1e-20, 0.99995));
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
     * @param energyScheme how the Energy is calculated
     * @param energyStack the stack on which features are applied to calcualte the Energy
     * @param logToConsole iff TRUE prints ongoing optimziation progress to console (useful for
     *     debugging)
     * @return the optimal state according to the algorithm
     * @throws OptTerminatedEarlyException
     * @throws CreateException
     */
    public static VoxelizedMarksWithEnergy findOpt(
            OptScheme<VoxelizedMarksWithEnergy, VoxelizedMarksWithEnergy> optScheme,
            KernelProposer<VoxelizedMarksWithEnergy> kernelProposer,
            MarkFactory markFactory,
            EnergySchemeWithSharedFeatures energyScheme,
            EnergyStack energyStack,
            boolean logToConsole)
            throws OptTerminatedEarlyException, CreateException {

        return optScheme.findOpt(
                kernelProposer,
                new ListUpdatableMarkSetCollection(),
                logToConsole ? new ConsoleAggregatedReporter(2) : new NullReporter<>(),
                createContext(
                        markFactory,
                        energyScheme,
                        energyStack,
                        BoundIOContextFixture.withSuppressedLogger()));
    }

    private static OptSchemeContext createContext(
            MarkFactory markFactory,
            EnergySchemeWithSharedFeatures energyScheme,
            EnergyStack energyStack,
            BoundIOContext context)
            throws CreateException {

        MarkWithIdentifierFactory factoryWithidentifier =
                new MarkWithIdentifierFactory(markFactory);
        BeanTestChecker.checkAndInit(
                factoryWithidentifier, NullInitParams.instance(), context.getLogger());

        // Uses a fixed random-seed so tests always generate the same result
        return new OptSchemeContext(
                "testExperiment",
                energyScheme,
                new DualStack(energyStack),
                new TriggerTerminationCondition(),
                context,
                new RandomNumberGeneratorMersenne(true),
                factoryWithidentifier);
    }
}
