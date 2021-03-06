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

import static org.junit.jupiter.api.Assertions.*;

import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.mpp.bean.bound.BoundUnitless;
import org.anchoranalysis.mpp.bean.mark.bounds.RotationBounds2D;
import org.anchoranalysis.mpp.bean.mark.bounds.RotationBounds3D;
import org.anchoranalysis.mpp.bean.mark.factory.MarkEllipseFactory;
import org.anchoranalysis.mpp.bean.mark.factory.MarkEllipsoidFactory;
import org.anchoranalysis.mpp.bean.mark.factory.MarkFactory;
import org.anchoranalysis.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.feature.mark.UpdatableMarksList;
import org.anchoranalysis.mpp.segment.bean.optimization.OptimizationScheme;
import org.anchoranalysis.mpp.segment.optimization.OptimizationTerminatedEarlyException;
import org.anchoranalysis.plugin.mpp.bean.proposer.mark.single.OrientationAndRadiiProposer;
import org.anchoranalysis.plugin.mpp.bean.proposer.orientation.RandomOrientation;
import org.anchoranalysis.plugin.mpp.bean.proposer.radii.UniformRandomRadiiProposer;
import org.anchoranalysis.plugin.mpp.segment.bean.optimization.extract.FromVoxelizedMarksWithEnergy;
import org.anchoranalysis.test.image.EnergyStackFixture;
import org.junit.jupiter.api.Test;

class SimulatedAnnealingTest {

    @Test
    void testOverlappingEllipses()
            throws OptimizationTerminatedEarlyException, CreateException, InitException,
                    BeanMisconfiguredException {
        runTest(false, 26, 4847.0, false);
    }

    @Test
    void testOverlappingEllipsoids()
            throws OptimizationTerminatedEarlyException, CreateException, InitException,
                    BeanMisconfiguredException {
        runTest(true, 18, 4159, false);
    }

    /**
     * Runs a simulated-annealing test with birth-and-death of a kind of marks, using a
     * energy-scheme that penalizes overlap.
     *
     * @param use3D iff true ellipsoids and a 3D scene are used, otherwise ellipses and a 2D scene
     * @param logToConsole if true log messages are printed to the console as the test runs (useful
     *     for debugging), otherwise suppressed
     * @throws OptimizationTerminatedEarlyException
     * @throws CreateException
     * @throws InitException
     */
    private void runTest(
            boolean use3D, int expectedSize, double expectedEnergy, boolean logToConsole)
            throws OptimizationTerminatedEarlyException, CreateException, InitException {

        OptimizationScheme<VoxelizedMarksWithEnergy, VoxelizedMarksWithEnergy, UpdatableMarksList>
                scheme =
                        OptimizationSchemeFixture.simulatedAnnealing(
                                new FromVoxelizedMarksWithEnergy(),
                                50 // A low number of iterations so that the test runs relatively
                                // quickly
                                );

        MarkFactory factory = use3D ? new MarkEllipsoidFactory() : new MarkEllipseFactory();

        VoxelizedMarksWithEnergy optimum =
                OptimizationSchemeFixture.findOptimum(
                        scheme,
                        KernelProposerFixture.createBirthAndDeath(
                                radiiAndOrientationProposer(use3D)),
                        factory,
                        EnergySchemeFixture.sizeMinusWeightedOverlap(-2),
                        EnergyStackFixture.create(true, use3D),
                        logToConsole);

        assertEquals(expectedSize, optimum.getMarks().size(), "number of marks");
        assertEquals(expectedEnergy, optimum.getEnergyTotal(), 1e-1, "energy");
    }

    private static MarkProposer radiiAndOrientationProposer(boolean use3D) {
        return new OrientationAndRadiiProposer(
                new UniformRandomRadiiProposer(new BoundUnitless(2, 5), use3D),
                new RandomOrientation(use3D ? new RotationBounds3D() : new RotationBounds2D()));
    }
}
