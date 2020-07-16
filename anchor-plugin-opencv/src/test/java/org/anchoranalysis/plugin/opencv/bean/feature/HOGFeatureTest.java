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
/* (C)2020 */
package org.anchoranalysis.plugin.opencv.bean.feature;

import static org.junit.Assert.*;

import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.size.SizeXY;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.junit.Test;

public class HOGFeatureTest {

    private TestLoaderImageIO testLoader =
            new TestLoaderImageIO(TestLoader.createFromMavenWorkingDirectory());

    private NRGStack stack = new NRGStack(testLoader.openStackFromTestPath("car.jpg"));

    @Test
    public void testWithinBounds() throws FeatureCalcException {
        assertEquals(0.006448161, featureValForIndex(0), 10e-6);
    }

    @Test(expected = FeatureCalcException.class)
    public void testOutsideBounds() throws FeatureCalcException {
        featureValForIndex(60000);
    }

    private double featureValForIndex(int index) throws FeatureCalcException {

        HOGFeature feature = new HOGFeature();
        feature.setResizeTo(new SizeXY(64, 64));
        feature.setIndex(index);

        FeatureCalculatorSingle<FeatureInputStack> session =
                FeatureSession.with(feature, LoggingFixture.suppressedLogErrorReporter());
        return session.calc(new FeatureInputStack(stack));
    }
}
