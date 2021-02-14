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
import org.anchoranalysis.feature.energy.EnergyStackWithoutParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.feature.input.FeatureInputStack;
import org.anchoranalysis.plugin.opencv.test.ImageLoader;
import org.anchoranalysis.test.LoggingFixture;
import org.junit.jupiter.api.Test;

class HOGFeatureTest {

    private ImageLoader loader = new ImageLoader();

    private static final double DELTA = 10e-6;

    @Test
    void testRGB() throws FeatureCalculationException {
        assertFeatureValueFirst(loader.carRGBAsEnergy(), 0.01632116);
    }

    @Test
    void testGrayscale8bit() throws FeatureCalculationException {
        assertFeatureValueFirst(loader.carGrayscale8BitAsEnergy(), 00.03600078);
    }

    @Test
    void testGrayscale16bit() throws FeatureCalculationException {
        assertFailure(loader.carGrayscale16BitAsEnergy(), 0);
    }

    @Test
    void testOutsideBounds() throws FeatureCalculationException {
        assertFailure(loader.carRGBAsEnergy(), 60000);
    }

    private void assertFailure(EnergyStackWithoutParams stack, int index) {
        assertThrows(
                FeatureCalculationException.class,
                () -> featureValueForIndex(loader.carRGBAsEnergy(), 60000));
    }

    private void assertFeatureValueFirst(EnergyStackWithoutParams stack, double expectedValue)
            throws FeatureCalculationException {
        assertEquals(expectedValue, featureValueForIndex(stack, 0), DELTA);
    }

    private double featureValueForIndex(EnergyStackWithoutParams stack, int index)
            throws FeatureCalculationException {
        HOGFeature feature = new HOGFeature(new SizeXY(64, 64), index);
        return FeatureSession.calculateWith(
                feature, new FeatureInputStack(stack), LoggingFixture.suppressedLogger());
    }
}
