package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi.FlexiFeatureTable;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi.MergedPairs;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.feature.plugins.FeaturesFromXmlFixture;

import ch.ethz.biol.cell.imageprocessing.objmask.provider.ObjMaskProviderReference;

class ExportFeaturesObjMaskTaskFixture {

	private TestLoader loader;
	
	public ExportFeaturesObjMaskTaskFixture(TestLoader loader) {
		this.loader = loader;
	}
	
	public <T extends FeatureInput> ExportFeaturesObjMaskTask<T> createTask(
		String pathFeatures,
		FlexiFeatureTable<T> selectFeaturesObjects,
		NRGStack nrgStack
	) throws CreateException {
				
		ExportFeaturesObjMaskTask<T> task = new ExportFeaturesObjMaskTask<>();

		task.setListFeaturesObjMask(
			loadFeatures(loader, pathFeatures)
		);
		task.setNrgStackProvider(
			nrgStackProvider(nrgStack)
		);
		task.setSelectFeaturesObjects(
			selectFeaturesObjects
		);
		task.setListObjMaskProvider(
			createObjProviders(MultiInputFixture.OBJS_NAME)
		);
		return task;
	}
	
	public MergedPairs createMergedPairs( Optional<String> pathPairFeatures ) throws CreateException {
		MergedPairs mergedPairs = new MergedPairs();
		mergedPairs.setSuppressErrors(true);
		if (pathPairFeatures.isPresent()) {
			mergedPairs.setListFeaturesPair(
				loadFeatures(loader, pathPairFeatures.get())
			);
		}
		return mergedPairs;
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
	
	/** creates a feature-list associated with obj-mask
	 *  
	 * @throws CreateException 
	 * */
	private static <T extends FeatureInput> List<NamedBean<FeatureListProvider<T>>> loadFeatures( TestLoader loader, String pathFeatureList ) throws CreateException {
		return FeaturesFromXmlFixture.createNamedFeatureProviders(pathFeatureList, loader);
	}
}
