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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi.FlexiFeatureTable;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi.MergedPairs;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi.Simple;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.feature.plugins.FeaturesFromXmlFixture;
import org.anchoranalysis.test.image.NRGStackFixture;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.mockito.Mockito.*;

import ch.ethz.biol.cell.imageprocessing.objmask.provider.ObjMaskProviderReference;


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
	
	private static ExportFeaturesObjMaskTaskFixture taskFixture;

	private static final String PATH_FEATURES = "singleFeatures.xml";
	private static final String PATH_FEATURES_SHELL = "singleFeaturesWithShell.xml";
	private static final String PATH_FEATURES_PAIR = "pairFeatures.xml";
	
	private static final String RELATIVE_PATH_SAVED_RESULTS = "expectedOutput/exportFeaturesObjMask/";
	
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
		TestLoader loader = TestLoader.createFromMavenWorkingDir();
		taskFixture = new ExportFeaturesObjMaskTaskFixture(loader);
	}
	
	@Test(expected=OperationFailedException.class)
	public void testSimpleSmall() throws OperationFailedException, CreateException {
		// The saved directory is irrelevant because an exception is thrown
		testOnTask(
			false,
			false,
			false,
			false,
			"irrelevant"
		);
	}
	
	@Test
	public void testSimpleLarge() throws OperationFailedException, CreateException {
		testOnTask(
			false,
			false,
			false,
			true,
			"simple01/"
		);
	}
	
	@Test(expected=OperationFailedException.class)
	public void testMergedSmall() throws OperationFailedException, CreateException {
		// The saved directory is irrelevant because an exception is thrown
		testOnTask(
			false,
			true,
			false,
			false,
			"irrelevant"
		);
	}
	
	@Test
	public void testMergedLarge() throws OperationFailedException, CreateException {
		testOnTask(
			false,
			true,
			false,
			true,
			"mergedPairs01/"
		);
	}
	
	@Test
	public void testMergedLargeWithPairs() throws OperationFailedException, CreateException {
		testOnTask(
			true,
			true,
			true,
			true,
			"mergedPairs02/"
		);
	}
	
	/**
	 * Runs a test to check if the results of ExportFeaturesObjMaskTask correspond to saved-values
	 * 
	 * @param singleIncludeShell iff TRUE additional an additional "shell" feature is included in the basic features, alongside the existing ones
	 * @param mergedPairs iff TRUE a merged-pairs table is created instead of a simple one
	 * @param includeFeaturesInPair iff TRUE pair-type features are included in the merged-pairs (only meaningful if mergedPairs==TRUE)
	 * @param bigSizeNrg iff TRUE a bigger NRG size is used, otherwise a small NRG size is used which deliberately causes various errors
	 * @param suffixPathDirSaved a suffix to identify where to find the saved-output to compare against
	 * @throws OperationFailedException
	 * @throws CreateException
	 */
	private void testOnTask(
		boolean singleIncludeShell,
		boolean mergedPairs,
		boolean includeFeaturesInPair,	// Only meaningful when mergePairs==true
		boolean bigSizeNrg,
		String suffixPathDirSaved
	) throws OperationFailedException, CreateException {
		
		String pathFeatures = singleIncludeShell ? PATH_FEATURES_SHELL : PATH_FEATURES; 
		
		NRGStack nrgStack = NRGStackFixture.create(bigSizeNrg, false).getNrgStack();
		
		testOnTask(
			pathFeatures,
			createFlexiFeature(mergedPairs, includeFeaturesInPair),
			nrgStack,
			suffixPathDirSaved
		);
	}
	
	private FlexiFeatureTable<?> createFlexiFeature(
		boolean mergedPairs,
		boolean includeFeaturesInPair	// Only meaningful when mergePairs==true
	) throws CreateException {
		if (mergedPairs) {
			return taskFixture.createMergedPairs(
				includeFeaturesInPair ? Optional.of(PATH_FEATURES_PAIR) : Optional.empty()
			); 
		} else {
			return new Simple();
		}
	}
	
	private void testOnTask(
		String pathFeatures,
		FlexiFeatureTable<?> selectFeaturesObjects,
		NRGStack nrgStack,
		String suffixPathDirSaved
	) throws OperationFailedException, CreateException {
		
		TaskSingleInputHelper.runTaskAndCompareOutputs(
			MultiInputFixture.createInput(nrgStack),
			taskFixture.createTask(
				pathFeatures,
				selectFeaturesObjects,
				nrgStack
			),
			folder.getRoot().toPath(),
			RELATIVE_PATH_SAVED_RESULTS + suffixPathDirSaved,
			OUTPUTS_TO_COMPARE
		);	
	}
}
