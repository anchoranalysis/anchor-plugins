package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi.FlexiFeatureTable;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi.MergedPairs;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.NRGStackFixture;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi.Simple;

import ch.ethz.biol.cell.imageprocessing.objmask.provider.ObjMaskProviderReference;

class ExportFeaturesObjMaskTaskFixture {

	private static final String PATH_FEATURES_SINGLE_DEFAULT = "singleFeatures.xml";
	private static final String PATH_FEATURES_SINGLE_WITH_SHELL = "singleFeaturesWithShell.xml";
	private static final String PATH_FEATURES_PAIR_DEFAULT = "pairFeatures.xml";
	
	private NRGStack nrgStack = createNRGStack(true);
	private FlexiFeatureTable<?> flexiFeatureTable = new Simple();
	
	/** The "single" and "pair" features in use.*/
	private FeatureListProviderFixture<FeatureInputSingleObj> singleFeatures;
	private FeatureListProviderFixture<FeatureInputPairObjs> pairFeatures;
	
	/**
	 * Constructor
	 * 
	 * <p>By default, use a big-sized NRG-stack that functions with our feature-lists</p>
	 * <p>By default, load the features from PATH_FEATURES</p>
	 * <p>By default, use Simple feature-mode. It can be changed to Merged-Pairs.</p>
	 * 
	 * @param loader
	 * @throws CreateException 
	 */
	public ExportFeaturesObjMaskTaskFixture(TestLoader loader) throws CreateException {
		this.nrgStack = createNRGStack(true);
		this.singleFeatures = new FeatureListProviderFixture<>(loader, PATH_FEATURES_SINGLE_DEFAULT);
		this.pairFeatures = new FeatureListProviderFixture<>(loader, PATH_FEATURES_PAIR_DEFAULT);
	}
	
	/** 
	 * Change to using a small nrg-stack that causes some features to throw errors
	 * */
	public void useSmallNRGInstead() {
		this.nrgStack = createNRGStack(false);
	}
	
	/** 
	 * Additionally include a shell feature in the "single" features
	 *  
	 * @throws CreateException */
	public void includeAdditionalShellFeature() throws CreateException {
		singleFeatures.useAlternativeXMLList(PATH_FEATURES_SINGLE_WITH_SHELL);
	}
	
	/** 
	 * Uses this feature instead of whatever list has been loaded for the single-features
	 * 
	 * <p>It does not initialize the feature.</p>
	 * */
	public void useInsteadAsSingleFeature( Feature<FeatureInputSingleObj> feature ) {
		singleFeatures.useSingleFeature(feature);
	}

	/** 
	 * Uses this feature instead of whatever list has been loaded for the pair-features
	 * 
	 * <p>It does not initialize the feature.</p>
	 * */
	public void useInsteadAsPairFeature( Feature<FeatureInputPairObjs> feature ) {
		pairFeatures.useSingleFeature(feature);
	}
	
	/** 
	 * Change to use Merged-Pairs mode rather than Simple mode
	 *
	 * @param includeFeaturesInPair iff TRUE "pair" features are populated in merged-pair mode
	 * @throws CreateException 
	 **/
	public void changeToMergedPairs(boolean includeFeaturesInPair) throws CreateException {
		flexiFeatureTable = createMergedPairs(includeFeaturesInPair);
	}
	
	public NRGStack getNrgStack() {
		return nrgStack;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends FeatureInput> ExportFeaturesObjMaskTask<T> createTask() throws CreateException {
				
		ExportFeaturesObjMaskTask<T> task = new ExportFeaturesObjMaskTask<>();
		task.setListFeaturesObjMask(
			singleFeatures.asListNamedBeansProvider()
		);
		task.setNrgStackProvider(
			nrgStackProvider(nrgStack)
		);
		
		task.setSelectFeaturesObjects( (FlexiFeatureTable<T>) flexiFeatureTable);
		task.setListObjMaskProvider(
			createObjProviders(MultiInputFixture.OBJS_NAME)
		);
		return task;
	}
		
	private static List<NamedBean<ObjMaskProvider>> createObjProviders(String objsName) {
		return Arrays.asList(
			new NamedBean<>(objsName, new ObjMaskProviderReference(objsName))	
		);
	}

	private static StackProvider nrgStackProvider(NRGStack nrgStack) throws CreateException {

		// Create NRG stack 
		Stack stack = nrgStack.asStack();
		
		// Encapsulate in a mock
		StackProvider stackProvider = mock(StackProvider.class);
		when(stackProvider.create()).thenReturn(stack);
		when(stackProvider.duplicateBean()).thenReturn(stackProvider);
		return stackProvider;
	}

	private MergedPairs createMergedPairs(boolean includeFeaturesInPair) throws CreateException {
		MergedPairs mergedPairs = new MergedPairs();
		mergedPairs.setSuppressErrors(true);
		if (includeFeaturesInPair) {
			mergedPairs.setListFeaturesPair(
				pairFeatures.asListNamedBeansProvider()
			);
		}
		return mergedPairs;
	}
	
	private NRGStack createNRGStack( boolean bigSizeNrg ) {
		return NRGStackFixture.create(bigSizeNrg, false).getNrgStack();
	}
}
