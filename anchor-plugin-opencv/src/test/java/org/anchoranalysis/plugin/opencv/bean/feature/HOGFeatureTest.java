/*-
 * #%L
 * anchor-plugin-opencv
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

package org.anchoranalysis.plugin.opencv.bean.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParameters;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.feature.input.FeatureInputStack;
import org.anchoranalysis.io.imagej.bean.interpolator.ImageJ;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.image.load.CarImageLoader;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link HOGFeature}.
 * 
 * @author Owen Feehan
 *
 */
class HOGFeatureTest {

    private CarImageLoader loader = new CarImageLoader();

    private static final double DELTA = 10e-6;

    @Test
    void testRGB() throws FeatureCalculationException {
        assertFeatureValueFirst(loader.carRGBAsEnergy(), 0.001620417227);
    }

    @Test
    void testGrayscale8bit() throws FeatureCalculationException {
        assertFeatureValueFirst(loader.carGrayscale8BitAsEnergy(), 0.0027965);
    }

    @Test
    void testGrayscale16bit() throws FeatureCalculationException {
        assertFailure(loader.carGrayscale16BitAsEnergy(), 0);
    }

    @Test
    void testOutsideBounds() throws FeatureCalculationException {
        assertFailure(loader.carRGBAsEnergy(), 60000);
    }

    private void assertFailure(EnergyStackWithoutParameters stack, int index) {
        assertThrows(
                FeatureCalculationException.class,
                () -> featureValueForIndex(loader.carRGBAsEnergy(), 60000));
    }

    private void assertFeatureValueFirst(EnergyStackWithoutParameters stack, double expectedValue)
            throws FeatureCalculationException {
        assertEquals(expectedValue, featureValueForIndex(stack, 0), DELTA);
    }

    private double featureValueForIndex(EnergyStackWithoutParameters stack, int index)
            throws FeatureCalculationException {
        
        HOGFeature feature = new HOGFeature(new SizeXY(64, 64), index);
        // Need to explicitly set the default value for the interpolator
        feature.setParameters( new HOGParameters( new ImageJ() ) );
        
        return FeatureSession.calculateWith(
                feature, new FeatureInputStack(stack), LoggingFixture.suppressedLogger());
    }
}
