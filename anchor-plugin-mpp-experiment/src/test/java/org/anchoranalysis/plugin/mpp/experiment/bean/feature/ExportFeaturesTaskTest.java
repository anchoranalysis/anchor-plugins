package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.pair.First;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.plugin.image.feature.bean.nrg.dimensions.Extent;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.feature.plugins.mockfeature.MockFeatureWithCalculationFixture;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.mockito.Mockito.*;

import java.util.function.Consumer;
import static org.anchoranalysis.plugin.mpp.experiment.bean.feature.ExportOutputter.*;

/**
 * Tests running {#link ExportFeaturesTask} on objects (both single and pairs)
 * 
 * <p>Two types of NRG stack are supported: big and small</p>
 * <p>For the small NRG stack, some of the object-masks are outside the scene size</p>
 * 
 * @author owen
 *
 */
public class ExportFeaturesTaskTest {
	
	private static TestLoader loader;
	private TaskFixture taskFixture;
	
	private static final String RELATIVE_PATH_SAVED_RESULTS = "expectedOutput/exportFeaturesObjMask/";
		
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@BeforeClass
	public static void setup() {
		RegisterBeanFactories.registerAllPackageBeanFactories();
		loader = TestLoader.createFromMavenWorkingDir();
	}
	
	@Before
	public void setupTest() throws CreateException {
		taskFixture = new TaskFixture(loader);
	}
	
	@Test(expected=OperationFailedException.class)
	public void testSimpleSmall() throws OperationFailedException {
		// The saved directory is irrelevant because an exception is thrown
		testOnTask(
			OUTPUT_DIR_IRRELEVANT,
			fixture -> fixture.useSmallNRGInstead()
		);
	}
	
	@Test
	public void testSimpleLarge() throws OperationFailedException {
		testOnTask(
			OUTPUT_DIR_SIMPLE_1,
			fixture -> {}	// Change nothing
		);
	}
	
	@Test(expected=OperationFailedException.class)
	public void testMergedSmall() throws OperationFailedException, CreateException {
		// The saved directory is irrelevant because an exception is thrown
		testOnTask(
			OUTPUT_DIR_IRRELEVANT,
			fixture -> {
				fixture.useSmallNRGInstead();
				fixture.changeToMergedPairs(false, false);		
			}
		);
	}
	
	@Test
	public void testMergedLarge() throws OperationFailedException, CreateException {
		testOnTask(
			OUTPUT_DIR_MERGED_1,
			fixture -> fixture.changeToMergedPairs(false, false) 
		);
	}
	
	@Test
	public void testMergedLargeWithPairs() throws OperationFailedException, CreateException {
		testOnTask(
			OUTPUT_DIR_MERGED_2,
			fixture -> {
				fixture.featureLoader().changeSingleToShellFeatures();
				fixture.changeToMergedPairs(true, false);		
			}
		);
	}
	
	@Test
	public void testMergedLargeWithImage() throws OperationFailedException, CreateException {
		testOnTask(
			OUTPUT_DIR_MERGED_3,
			fixture -> fixture.changeToMergedPairs(false, true) 
		);
	}
	
	/**
	 * Tests that the image-features are cached, and not repeatedly-calculated for the same image.
	 * 
	 * @throws OperationFailedException
	 * @throws CreateException
	 * @throws FeatureCalcException 
	 */
	@Test
	public void testCachingImageFeatures() throws OperationFailedException, CreateException, FeatureCalcException {
		
		@SuppressWarnings("unchecked")
		Extent<FeatureInputStack> feature = (Extent<FeatureInputStack>) spy(Extent.class);
		
		// To make sure we keep on using the spy, even after an expected duplication()
		when(feature.duplicateBean()).thenReturn(feature);
		
		testOnTask(
			OUTPUT_DIR_IMAGE_CACHE,
			fixture -> {
				fixture.featureLoader().changeImageTo(feature);
				fixture.changeToMergedPairs(false, true);		
			}
		);
		
		// If caching is working, then the feature should be calculated exactly once
		verify(feature, times(1)).calc(any());
	}
	
	/**
	 *  Tests when a particular FeatureCalculation is called by a feature in both the Single and Pair part of merged-pairs.
	 * 
	 *  <div>
	 *  There are 4 unique objects and 3 pairs of neighbors. For each pair, the feature is calculated on:
	 *  <ol>
	 *  <li>the left-object of the pair</li>
	 *  <li>the right-object of the pair</li>
	 *  <li>again the left-object of the pair (embedded in a FromFirst)</li>
	 *  <li>the merged-object</li>
	 *  </ol>
	 *  </div>
	 *  
	 *  <p>So the outputting feature table is 3 rows x 4 (result) columns.</p>
	 *  
	 *  <p>In a maximally-INEFFICIENT implementation (no caching),
	 *         the calculation would occur 12 times (3 pairs x 4 calculations each time)</p>
	 *  <p>In a maximally-EFFICIENT implementation (caching everything possible),
	 *         the calculation would occur only 7 times (once for each single-object and once for each merged object)</p>.
	 * 
	 * @throws OperationFailedException
	 * @throws CreateException
	 */
	@Test
	public void testRepeatedCalculationInSingleAndPair() throws OperationFailedException, CreateException {
		
		Feature<FeatureInputSingleObject> feature = MockFeatureWithCalculationFixture.createMockFeatureWithCalculation();
		
		taskFixture.featureLoader().changeSingleTo(feature);
		taskFixture.featureLoader().changePairTo(
			// This produces the same result as the feature calculated on the left-object
			new First(feature)	
		);
		taskFixture.changeToMergedPairs(true, false);
		
		MockFeatureWithCalculationFixture.executeAndAssertCnt(
			// Each "single" input calculated once (as the results are cached), and twice for each pair (for pair and merged)
			MultiInputFixture.NUM_INTERSECTING_OBJECTS + (2 * MultiInputFixture.NUM_PAIRS_INTERSECTING),
			 // a calculation for each single object, and a calculation for each merged object
			(MultiInputFixture.NUM_PAIRS_INTERSECTING + MultiInputFixture.NUM_INTERSECTING_OBJECTS),
			() -> testOnTask("repeatedInSingleAndPair/")
		);
	}

	/** Calculate with a reference to another feature included in the list */
	@Test
	public void testSimpleLargeWithIncludedReference() throws OperationFailedException, CreateException {
		testOnTask(
			OUTPUT_DIR_SIMPLE_WITH_REFERENCE,
			fixture -> fixture.featureLoader().changeSingleToReferenceWithInclude() 
		);
	}
	
	/** Calculate with a reference to a feature that exists among the shared features */
	@Test
	public void testSimpleLargeWithSharedReference() throws OperationFailedException, CreateException {
		testOnTask(
			OUTPUT_DIR_SIMPLE_WITH_REFERENCE,
			fixture -> fixture.featureLoader().changeSingleToReferenceShared()
		);
	}
	
	private void testOnTask( String outputDir, Consumer<TaskFixture> changeFixture) throws OperationFailedException {
		changeFixture.accept(taskFixture);
		testOnTask(outputDir);
	}
	
	/**
	 * Runs a test to check if the results of ExportFeaturesObjMaskTask correspond to saved-values
	 *
	 * @param suffixPathDirSaved a suffix to identify where to find the saved-output to compare against
	 * @throws OperationFailedException
	 * @throws CreateException
	 */
	private void testOnTask(String suffixPathDirSaved) throws OperationFailedException {
		
		try {
			TaskSingleInputHelper.runTaskAndCompareOutputs(
				MultiInputFixture.createInput( taskFixture.getNrgStack() ),
				taskFixture.createTask(),
				folder.getRoot().toPath(),
				RELATIVE_PATH_SAVED_RESULTS + suffixPathDirSaved,
				OUTPUTS_TO_COMPARE
			);
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}	
	}
}