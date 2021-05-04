/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import static org.anchoranalysis.plugin.mpp.experiment.bean.feature.ExportOutputter.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParams;
import org.anchoranalysis.image.feature.bean.object.pair.First;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.input.FeatureInputStack;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.image.feature.bean.dimensions.Extent;
import org.anchoranalysis.test.feature.plugins.mockfeature.MockFeatureWithCalculationFixture;
import org.junit.jupiter.api.Test;

/**
 * Tests running {#link ExportFeaturesTask} on objects (both single and pairs)
 *
 * <p>Two types of energy stack are supported: big and small
 *
 * <p>For the small energy stack, some of the object-masks are outside the scene size
 *
 * @author Owen Feehan
 */
class ExportFeaturesObjectTest
        extends ExportFeaturesTestBase<MultiInput, FeatureInputSingleObject, TaskFixtureObjects> {

    private static final String EXPECTED_OUTPUT_SUBDIRECTORY = "object";

    ExportFeaturesObjectTest() {
        super(EXPECTED_OUTPUT_SUBDIRECTORY, true, TaskFixtureObjects::new);
    }

    @Test
    void testSimpleSmall() throws OperationFailedException {
        assertThrows(
                OperationFailedException.class,
                () ->
                        // The saved directory is irrelevant because an exception is thrown
                        testOnTask(OUTPUT_DIR_IRRELEVANT, TaskFixture::useSmallEnergyInstead));
    }

    @Test
    void testSimpleLarge() throws OperationFailedException {
        testOnTask(
                OUTPUT_DIRECTORY_SIMPLE_1, fixture -> {} // Change nothing
                );
    }

    @Test
    void testMergedSmall() {
        // The saved directory is irrelevant because an exception is thrown
        assertThrows(
                OperationFailedException.class,
                () ->
                        testOnTask(
                                OUTPUT_DIR_IRRELEVANT,
                                fixture -> {
                                    fixture.useSmallEnergyInstead();
                                    fixture.changeToMergedPairs(false, false);
                                }));
    }

    @Test
    void testMergedLarge() throws OperationFailedException, CreateException {
        testOnTask(OUTPUT_DIRECTORY_MERGED_1, fixture -> fixture.changeToMergedPairs(false, false));
    }

    @Test
    void testMergedLargeWithPairs() throws OperationFailedException, CreateException {
        testOnTask(
                OUTPUT_DIRECTORY_MERGED_2,
                fixture -> {
                    fixture.featureLoader().changeSingleToShellFeatures();
                    fixture.changeToMergedPairs(true, false);
                });
    }

    @Test
    void testMergedLargeWithImage() throws OperationFailedException, CreateException {
        testOnTask(OUTPUT_DIRECTORY_MERGED_3, fixture -> fixture.changeToMergedPairs(false, true));
    }

    /**
     * Tests that the image-features are cached, and not repeatedly-calculated for the same image.
     *
     * @throws OperationFailedException
     * @throws CreateException
     * @throws FeatureCalculationException
     */
    @Test
    void testCachingImageFeatures()
            throws OperationFailedException, CreateException, FeatureCalculationException {

        @SuppressWarnings("unchecked")
        Extent<FeatureInputStack> feature = (Extent<FeatureInputStack>) spy(Extent.class);

        // To make sure we keep on using the spy, even after an expected duplication()
        when(feature.duplicateBean()).thenReturn(feature);

        testOnTask(
                OUTPUT_DIRECTORY_IMAGE_CACHE,
                fixture -> {
                    fixture.featureLoader().changeImageTo(feature);
                    fixture.changeToMergedPairs(false, true);
                });

        // If caching is working, then the feature should be calculated exactly once
        verify(feature, times(1)).calculate(any());
    }

    /**
     * Tests when a particular FeatureCalculation is called by a feature in both the Single and Pair
     * part of merged-pairs.
     *
     * <p><div> There are 4 unique objects and 3 pairs of neighbors. For each pair, the feature is
     * calculated on:
     *
     * <ol>
     *   <li>the left-object of the pair
     *   <li>the right-object of the pair
     *   <li>again the left-object of the pair (embedded in a FromFirst)
     *   <li>the merged-object
     * </ol>
     *
     * </div>
     *
     * <p>So the outputting feature table is 3 rows x 4 (result) columns.
     *
     * <p>In a maximally-INEFFICIENT implementation (no caching), the calculation would occur 12
     * times (3 pairs x 4 calculations each time)
     *
     * <p>In a maximally-EFFICIENT implementation (caching everything possible), the calculation
     * would occur only 7 times (once for each single-object and once for each merged object).
     *
     * @throws OperationFailedException
     * @throws CreateException
     */
    @Test
    void testRepeatedCalculationInSingleAndPair() throws OperationFailedException, CreateException {

        Feature<FeatureInputSingleObject> feature =
                MockFeatureWithCalculationFixture.createMockFeatureWithCalculation();

        taskFixture.featureLoader().changeSingleTo(feature);
        taskFixture
                .featureLoader()
                .changePairTo(
                        // This produces the same result as the feature calculated on the
                        // left-object
                        new First(feature));
        taskFixture.changeToMergedPairs(true, false);

        MockFeatureWithCalculationFixture.executeAndAssertCount(
                // Each "single" input calculated once (as the results are cached), and twice for
                // each pair (for pair and merged)
                MultiInputFixture.NUMBER_INTERSECTING_OBJECTS
                        + (2 * MultiInputFixture.NUMBER_PAIRS_INTERSECTING),
                // a calculation for each single object, and a calculation for each merged object
                (MultiInputFixture.NUMBER_PAIRS_INTERSECTING
                        + MultiInputFixture.NUMBER_INTERSECTING_OBJECTS),
                () -> testOnTask("repeatedInSingleAndPair/"));
    }

    /** Calculate with a reference to another feature included in the list */
    @Test
    void testSimpleLargeWithIncludedReference() throws OperationFailedException, CreateException {
        testOnTask(
                OUTPUT_DIRECTORY_SIMPLE_WITH_REFERENCE,
                fixture -> fixture.featureLoader().changeSingleToReferenceWithInclude());
    }

    /** Calculate with a reference to a feature that exists among the shared features */
    @Test
    void testSimpleLargeWithSharedReference() throws OperationFailedException, CreateException {
        testOnTask(
                OUTPUT_DIRECTORY_SIMPLE_WITH_REFERENCE,
                fixture -> fixture.featureLoader().changeSingleToReferenceShared());
    }

    @Override
    protected MultiInput createInput(EnergyStackWithoutParams stack) {
        return MultiInputFixture.createInput(stack);
    }
}
