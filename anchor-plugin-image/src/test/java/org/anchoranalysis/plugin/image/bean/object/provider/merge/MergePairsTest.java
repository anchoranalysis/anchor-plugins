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

package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import static org.anchoranalysis.plugin.image.bean.object.provider.merge.MergeTestHelper.*;

import java.nio.file.Path;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.shared.relation.GreaterThanBean;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.Constant;
import org.anchoranalysis.image.feature.bean.object.pair.Minimum;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.plugin.image.provider.ProviderFixture;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.feature.plugins.mockfeature.MockFeatureWithCalculationFixture;
import org.junit.Test;
import org.mockito.Mockito;

public class MergePairsTest {

    static {
        RegisterBeanFactories.registerAllPackageBeanFactories();
    }

    /**
     * Threshold chosen so all objects merge
     *
     * @throws OperationFailedException
     */
    @Test
    public void testAllMerge()
            throws BeanMisconfiguredException, CreateException, InitException,
                    OperationFailedException {
        testLinear(EXPECTED_RESULT_ALL_INTERSECTING_MERGED, 26, 14, 1);
    }

    /**
     * Threshold chosen so the first three objects are too small to merge
     *
     * @throws OperationFailedException
     */
    @Test
    public void testSomeMerge()
            throws BeanMisconfiguredException, CreateException, InitException,
                    OperationFailedException {
        testLinear(EXPECTED_RESULT_FIRST_THREE_NOT_MERGING, 22, 12, 300);
    }

    private void testLinear(
            int expectedCntMerged,
            int expectedFeatureCalcCount,
            int expectedCalculationCount,
            int threshold)
            throws OperationFailedException, CreateException {
        MergeTestHelper.testProviderOn(
                expectedCntMerged,
                expectedFeatureCalcCount,
                expectedCalculationCount,
                createMergePair(OBJECTS_LINEAR_INTERSECTING, threshold));
    }

    private static MergePairs createMergePair(ObjectCollection objects, int threshold)
            throws CreateException {

        Logger logger = LoggingFixture.suppressedLogErrorReporter();

        MergePairs provider = new MergePairs();

        Feature<FeatureInputPairObjects> feature =
                new Minimum(MockFeatureWithCalculationFixture.createMockFeatureWithCalculation());

        provider.setObjects(ProviderFixture.providerFor(objects));
        provider.setFeatureEvaluatorThreshold(
                FeatureEvaluatorFixture.createEnergy(
                        new Constant<>(threshold), logger, Mockito.mock(Path.class)));
        provider.setFeatureEvaluatorMerge(
                FeatureEvaluatorFixture.createEnergy(feature, logger, Mockito.mock(Path.class)));
        provider.setRelation(new GreaterThanBean());
        return provider;
    }
}
