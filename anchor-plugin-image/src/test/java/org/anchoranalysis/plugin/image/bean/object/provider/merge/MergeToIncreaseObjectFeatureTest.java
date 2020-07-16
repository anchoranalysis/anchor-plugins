/*-
 * #%L
 * anchor-plugin-image
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
package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import java.nio.file.Path;
import java.util.function.ToDoubleFunction;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.plugin.image.test.ProviderFixture;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.feature.plugins.mockfeature.MockFeatureWithCalculationFixture;
import org.junit.Test;
import org.mockito.Mockito;

public class MergeToIncreaseObjectFeatureTest {

    static {
        RegisterBeanFactories.registerAllPackageBeanFactories();
    }

    /**
     * This test will merge so long as the total number of pixels increases (so every possible
     * neighbor will eventually merge!)
     *
     * @throws OperationFailedException
     * @throws CreateException
     */
    @Test
    public void testMaximalNumPixels()
            throws BeanMisconfiguredException, InitException, OperationFailedException,
                    CreateException {
        testLinear(
                MergeTestHelper.EXPECTED_RESULT_ALL_INTERSECTING_MERGED,
                24,
                MockFeatureWithCalculationFixture.DEFAULT_FUNC_NUM_PIXELS);
    }

    /**
     * This tests will merge if it brings the total number of pixels closer to 500, but not merge
     * otherwise.
     *
     * <p>Therefore some neighboring objects will merge and others will not
     *
     * @throws OperationFailedException
     * @throws CreateException
     */
    @Test
    public void testConvergeNumPixels()
            throws BeanMisconfiguredException, InitException, OperationFailedException,
                    CreateException {
        testLinear(8, 23, MergeToIncreaseObjectFeatureTest::convergeTo900);
    }

    private void testLinear(
            int expectedCntMerged,
            int expectedFeatureCalcCount,
            ToDoubleFunction<FeatureInputSingleObject> calculationFunc)
            throws OperationFailedException, CreateException {
        MergeTestHelper.testProviderOn(
                expectedCntMerged,
                expectedFeatureCalcCount,
                expectedFeatureCalcCount, // Same as the feature-count as it should always be a new
                // unique object
                createMergeMax(MergeTestHelper.OBJECTS_LINEAR_INTERSECTING, calculationFunc));
    }

    private static MergeToIncreaseObjectFeature createMergeMax(
            ObjectCollection objects,
            ToDoubleFunction<FeatureInputSingleObject> calculationFunction)
            throws CreateException {

        Logger logger = LoggingFixture.suppressedLogErrorReporter();

        MergeToIncreaseObjectFeature provider = new MergeToIncreaseObjectFeature();

        provider.setObjects(ProviderFixture.providerFor(objects));
        provider.setFeatureEvaluator(
                FeatureEvaluatorFixture.createNrg(
                        MockFeatureWithCalculationFixture.createMockFeatureWithCalculation(
                                calculationFunction),
                        logger,
                        Mockito.mock(Path.class)));

        return provider;
    }

    /** Merges if the number-of-pixels becomes closer to 900 */
    private static Double convergeTo900(FeatureInputSingleObject input) {
        int diff = 900 - input.getObject().numberVoxelsOn();
        return (double) -1 * Math.abs(diff);
    }
}
