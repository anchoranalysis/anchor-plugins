package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi.Simple;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.feature.plugins.FeaturesFromXmlFixture;
import org.anchoranalysis.test.feature.plugins.NRGStackFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.mockito.Mockito.*;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderEmpty;
import ch.ethz.biol.cell.imageprocessing.objmask.provider.ObjMaskProviderReference;
import ch.ethz.biol.cell.imageprocessing.stack.provider.StackProviderChnlProvider;


public class ExportFeaturesObjMaskTaskTest {
	
	static {
		RegisterBeanFactories.registerAllPackageBeanFactories();
	}
	
	private static TestLoader loader = TestLoader.createFromMavenWorkingDir();
	
	private static final String[] OUTPUTS_TO_COMPARE = {
		"csvAgg.csv",
		"csvAll.csv",
		"arbitraryPath/objsTest/csvGroup.csv",
		"stackCollection/input.tif",
		"nrgStack/nrgStack_00.tif",
		"manifest.ser.xml",
		"nrgStackParams.xml",
		"arbitraryPath/objsTest/paramsGroupAgg.xml"			
	};
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testArbitraryParams() throws OperationFailedException, CreateException {
	
		TaskSingleInputHelper.runTaskAndCompareOutputs(
			MultiInputFixture.createInput(),
			createTask(),
			folder.getRoot().toPath(),
			"expectedOutput/exportFeaturesObjMask/simple01/",
			OUTPUTS_TO_COMPARE
		);
	}
	
	private static ExportFeaturesObjMaskTask createTask() throws CreateException {
		ExportFeaturesObjMaskTask task = new ExportFeaturesObjMaskTask();
		task.setListFeaturesObjMask( objMaskFeatures(loader) );
		task.setNrgStackProvider( nrgStackProvider() );
		task.setSelectFeaturesObjects( new Simple() );
		task.setListObjMaskProvider(
			createObjProviders(MultiInputFixture.OBJS_NAME)
		);
		return task;
	}
	
	private static List<NamedBean<ObjMaskProvider>> createObjProviders(String objsName) {
		List<NamedBean<ObjMaskProvider>> list = new ArrayList<>();
		list.add(
			new NamedBean<>(objsName, new ObjMaskProviderReference(objsName))
		);
		return list;
	}

	private static StackProvider nrgStackProvider() throws CreateException {

		// Create NRG stack 
		Stack stack = NRGStackFixture.create().getNrgStack().asStack();
		
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
	private static List<NamedBean<FeatureListProvider<FeatureObjMaskParams>>> objMaskFeatures( TestLoader loader ) throws CreateException {
		return FeaturesFromXmlFixture.createNamedFeatureProviders("namedObjMaskFeaturesList.xml", loader);
	}
}
