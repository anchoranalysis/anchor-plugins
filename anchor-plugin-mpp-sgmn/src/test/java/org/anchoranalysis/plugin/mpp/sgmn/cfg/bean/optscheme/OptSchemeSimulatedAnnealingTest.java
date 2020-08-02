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

import static org.junit.Assert.*;

import org.anchoranalysis.anchor.mpp.bean.bound.BoundUnitless;
import org.anchoranalysis.anchor.mpp.bean.mark.bounds.RotationBounds2D;
import org.anchoranalysis.anchor.mpp.bean.mark.bounds.RotationBounds3D;
import org.anchoranalysis.anchor.mpp.bean.mark.factory.MarkEllipseFactory;
import org.anchoranalysis.anchor.mpp.bean.mark.factory.MarkEllipsoidFactory;
import org.anchoranalysis.anchor.mpp.bean.mark.factory.MarkFactory;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.OptScheme;
import org.anchoranalysis.mpp.sgmn.optscheme.OptTerminatedEarlyException;
import org.anchoranalysis.plugin.mpp.bean.proposer.mark.OrientationAndRadiiProposer;
import org.anchoranalysis.plugin.mpp.bean.proposer.orientation.RandomOrientation;
import org.anchoranalysis.plugin.mpp.bean.proposer.radii.UniformRandomRadiiProposer;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.extractscoresize.CfgNRGPixelizedExtracter;
import org.anchoranalysis.test.image.NRGStackFixture;
import org.junit.Test;

public class OptSchemeSimulatedAnnealingTest {

    @Test
    public void testOverlappingEllipses()
            throws OptTerminatedEarlyException, CreateException, InitException,
                    BeanMisconfiguredException {
        runTest(false, 26, 4847.0, false);
    }

    @Test
    public void testOverlappingEllipsoids()
            throws OptTerminatedEarlyException, CreateException, InitException,
                    BeanMisconfiguredException {
        runTest(true, 18, 4159, false);
    }

    /**
     * Runs a simulated-annealing test with birth-and-death of a kind of marks, using a nrg-scheme
     * that penalizes overlap
     *
     * @param use3D iff TRUE ellipsoids and a 3D scene are used, otherwise ellipses and a 2D scene
     * @param logToConsole if TRUE log messages are printed to the console as the test runs (useful
     *     for debugging), otherwise suppressed
     * @throws OptTerminatedEarlyException
     * @throws CreateException
     * @throws InitException
     */
    private void runTest(boolean use3D, int expectedSize, double expectedNRG, boolean logToConsole)
            throws OptTerminatedEarlyException, CreateException, InitException {

        OptScheme<CfgNRGPixelized, CfgNRGPixelized> optScheme =
                OptSchemeFixture.simulatedAnnealing(
                        new CfgNRGPixelizedExtracter(),
                        50 // A low number of iterations so that the test runs relatively quickly
                        );

        MarkFactory factory = use3D ? new MarkEllipsoidFactory() : new MarkEllipseFactory();

        CfgNRGPixelized optima =
                OptSchemeFixture.findOpt(
                        optScheme,
                        KernelProposerFixture.createBirthAndDeath(
                                radiiAndOrientationProposer(use3D)),
                        factory,
                        NRGSchemeFixture.sizeMinusWeightedOverlap(-2),
                        NRGStackFixture.create(true, use3D),
                        logToConsole);

        assertEquals("number of marks", expectedSize, optima.getCfgNRG().getCfg().size());
        assertEquals("nrg", expectedNRG, optima.getCfgNRG().getNrgTotal(), 1e-1);
    }

    private static MarkProposer radiiAndOrientationProposer(boolean use3D) {
        return new OrientationAndRadiiProposer(
                new UniformRandomRadiiProposer(new BoundUnitless(2, 5), use3D),
                new RandomOrientation(use3D ? new RotationBounds3D() : new RotationBounds2D()));
    }
}
