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
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.plugin.image.feature.bean.obj.pair.order.First;
import org.anchoranalysis.test.TestLoader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.mockito.Mockito.*;

import ch.ethz.biol.cell.mpp.nrg.feature.stack.SceneWidth;

/**
 * Tests running {#link ExportFeaturesObjMaskTask} on a single input
 * 
 * <p>Two types of NRG stack are supported: big and small</p>
 * <p>For the small NRG stack, some of the object-masks are outside the scene size</p>
 * 
 * @author owen
 *
 */
public class ExportFeaturesObjMaskTaskTest {
	
	private static TestLoader loader;
	private ExportFeaturesObjMaskTaskFixture taskFixture;
		
	private static final String RELATIVE_PATH_SAVED_RESULTS = "expectedOutput/exportFeaturesObjMask/";
	
	// Saved output locations for particular tests
	private static final String OUTPUT_DIR_SIMPLE_1 = "simple01/";
	private static final String OUTPUT_DIR_MERGED_1 = "mergedPairs01/";
	private static final String OUTPUT_DIR_MERGED_2 = "mergedPairs02/";
	private static final String OUTPUT_DIR_MERGED_3 = "mergedPairs03/";
	private static final String OUTPUT_DIR_IMAGE_CACHE = "imageCache/";
	
	// Used for tests where we expect an exception to be thrown, and thus never to actually be compared
	// It doesn't physically exist
	private static final String OUTPUT_DIR_IRRELEVANT = "irrelevant/";
	
	private static final String[] OUTPUTS_TO_COMPARE = {
		"csvAgg.csv",
		"csvAll.csv",
		"arbitraryPath/objsTest/csvGroup.csv",
		"stackCollection/input.tif",
		"nrgStack/nrgStack_00.tif",
		"manifest.ser.xml",
		"nrgStackParams.xml",
		"arbitraryPath/objsTest/paramsGroupAgg.xml",
		"objMaskCollection/objsTest.h5"
	};
		
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@BeforeClass
	public static void setup() {
		RegisterBeanFactories.registerAllPackageBeanFactories();
		loader = TestLoader.createFromMavenWorkingDir();
	}
	
	@Before
	public void setupTest() throws CreateException {
		taskFixture = new ExportFeaturesObjMaskTaskFixture(loader);
	}
	
	@Test(expected=OperationFailedException.class)
	public void testSimpleSmall() throws OperationFailedException {
		// The saved directory is irrelevant because an exception is thrown
		taskFixture.useSmallNRGInstead();
		testOnTask(OUTPUT_DIR_IRRELEVANT);
	}
	
	@Test
	public void testSimpleLarge() throws OperationFailedException {
		testOnTask(OUTPUT_DIR_SIMPLE_1);
	}
	
	@Test(expected=OperationFailedException.class)
	public void testMergedSmall() throws OperationFailedException, CreateException {
		// The saved directory is irrelevant because an exception is thrown
		taskFixture.useSmallNRGInstead();
		taskFixture.changeToMergedPairs(false, false);
		testOnTask(OUTPUT_DIR_IRRELEVANT);
	}
	
	@Test
	public void testMergedLarge() throws OperationFailedException, CreateException {
		taskFixture.changeToMergedPairs(false, false);
		testOnTask(OUTPUT_DIR_MERGED_1);
	}
	
	@Test
	public void testMergedLargeWithPairs() throws OperationFailedException, CreateException {
		taskFixture.includeAdditionalShellFeature();
		taskFixture.changeToMergedPairs(true, false);
		testOnTask(OUTPUT_DIR_MERGED_2);
	}
	
	@Test
	public void testMergedLargeWithImage() throws OperationFailedException, CreateException {
		taskFixture.changeToMergedPairs(false, true);
		testOnTask(OUTPUT_DIR_MERGED_3);
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
		
		SceneWidth feature = spy(SceneWidth.class);
		
		// To make sure we keep on using the spy, even after an expected duplication()
		when(feature.duplicateBean()).thenReturn(feature);
		
		taskFixture.useInsteadAsImageFeature(feature);
		taskFixture.changeToMergedPairs(false, true);
		
		testOnTask(OUTPUT_DIR_IMAGE_CACHE);
		
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
		
		Feature<FeatureInputSingleObj> feature = new FeatureCalculationFixture.MockFeatureWithCalculation();
		
		taskFixture.useInsteadAsSingleFeature(feature);
		taskFixture.useInsteadAsPairFeature(
			// This produces the same result as the feature calculated on the left-object
			new First(feature)	
		);
		taskFixture.changeToMergedPairs(true, false);
		
		FeatureCalculationFixture.executeAndAssertCnt(
			 // a calculation for each single object, and a calculation for each merged object
			(MultiInputFixture.NUM_PAIRS_INTERSECTING + MultiInputFixture.NUM_INTERSECTING_OBJECTS),
			() -> testOnTask("repeatedInSingleAndPair/")
		);
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
