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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.initializable.params.NullInitParams;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.random.RandomNumberGeneratorMersenne;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.mpp.bean.mark.MarkWithIdentifierFactory;
import org.anchoranalysis.mpp.bean.mark.factory.MarkFactory;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.feature.energy.scheme.EnergySchemeWithSharedFeatures;
import org.anchoranalysis.mpp.feature.mark.UpdatableMarksList;
import org.anchoranalysis.mpp.segment.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.segment.bean.optimization.ExtractScoreSize;
import org.anchoranalysis.mpp.segment.bean.optimization.OptimizationScheme;
import org.anchoranalysis.mpp.segment.bean.optimization.termination.TriggerTerminationCondition;
import org.anchoranalysis.mpp.segment.optimization.DualStack;
import org.anchoranalysis.mpp.segment.optimization.OptimizationContext;
import org.anchoranalysis.mpp.segment.optimization.OptimizationTerminatedEarlyException;
import org.anchoranalysis.plugin.mpp.segment.bean.annealing.Geometry;
import org.anchoranalysis.plugin.mpp.segment.bean.optimization.mode.DirectAssignMode;
import org.anchoranalysis.plugin.mpp.segment.bean.optimization.reporter.ConsoleAggregatedReporter;
import org.anchoranalysis.plugin.mpp.segment.bean.optimization.reporter.NullReporter;
import org.anchoranalysis.plugin.mpp.segment.bean.optimization.termination.NumberIterations;
import org.anchoranalysis.test.experiment.BeanTestChecker;
import org.anchoranalysis.test.image.InputOutputContextFixture;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class OptimizationSchemeFixture {

    /** A simulated annealing scheme using direct-assign mode. */
    public static <T> OptimizationScheme<T, T, UpdatableMarksList> simulatedAnnealing(
            ExtractScoreSize<T> extractScoreSize, int numberOfIterations) {
        SimulatedAnnealing<T, T, T> optimization = new SimulatedAnnealing<>();
        optimization.setAnnealScheme(new Geometry(1e-20, 0.99995));
        optimization.setAssignMode(new DirectAssignMode<>(extractScoreSize));
        optimization.setTermination(new NumberIterations(numberOfIterations));
        return BeanTestChecker.check(optimization);
    }

    /**
     * Finds the marks-collection with the optimum energy (minimal/maximal).
     *
     * @param optimization optimization-scheme
     * @param kernelProposer kernel-proposer
     * @param markFactory creates a new mark when a "birth" occurs
     * @param energyScheme how the Energy is calculated
     * @param energyStack the stack on which features are applied to calculate the Energy
     * @param logToConsole iff true prints ongoing optimization progress to console (useful for
     *     debugging)
     * @return the optimal state according to the algorithm
     * @throws OptimizationTerminatedEarlyException
     * @throws CreateException
     */
    public static VoxelizedMarksWithEnergy findOptimum(
            OptimizationScheme<VoxelizedMarksWithEnergy, VoxelizedMarksWithEnergy, UpdatableMarksList> optimization,
            KernelProposer<VoxelizedMarksWithEnergy,UpdatableMarksList> kernelProposer,
            MarkFactory markFactory,
            EnergySchemeWithSharedFeatures energyScheme,
            EnergyStack energyStack,
            boolean logToConsole)
            throws OptimizationTerminatedEarlyException, CreateException {

        return optimization.findOptimum(
                kernelProposer,
                new UpdatableMarksList(),
                logToConsole ? new ConsoleAggregatedReporter(2) : new NullReporter<>(),
                createContext(
                        markFactory,
                        energyScheme,
                        energyStack,
                        InputOutputContextFixture.withSuppressedLogger()));
    }

    private static OptimizationContext createContext(
            MarkFactory markFactory,
            EnergySchemeWithSharedFeatures energyScheme,
            EnergyStack energyStack,
            InputOutputContext context)
            throws CreateException {

        MarkWithIdentifierFactory factoryWithidentifier =
                new MarkWithIdentifierFactory(markFactory);
        BeanTestChecker.checkAndInit(
                factoryWithidentifier, NullInitParams.instance(), context.getLogger());

        // Uses a fixed random-seed so tests always generate the same result
        return new OptimizationContext(
                "testExperiment",
                energyScheme,
                new DualStack(energyStack),
                new TriggerTerminationCondition(),
                context,
                new RandomNumberGeneratorMersenne(true),
                factoryWithidentifier);
    }
}
