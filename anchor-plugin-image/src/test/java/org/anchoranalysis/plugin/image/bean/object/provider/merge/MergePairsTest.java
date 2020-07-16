/* (C)2020 */
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
import org.anchoranalysis.plugin.image.test.ProviderFixture;
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
                FeatureEvaluatorFixture.createNrg(
                        new Constant<>(threshold), logger, Mockito.mock(Path.class)));
        provider.setFeatureEvaluatorMerge(
                FeatureEvaluatorFixture.createNrg(feature, logger, Mockito.mock(Path.class)));
        provider.setRelation(new GreaterThanBean());
        return provider;
    }
}
