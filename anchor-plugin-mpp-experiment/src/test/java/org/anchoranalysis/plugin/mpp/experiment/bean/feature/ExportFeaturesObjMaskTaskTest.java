package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi.Simple;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.feature.plugins.FeaturesFromXmlFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderEmpty;
import ch.ethz.biol.cell.imageprocessing.objmask.provider.ObjMaskProviderReference;
import ch.ethz.biol.cell.imageprocessing.stack.provider.StackProviderChnlProvider;

import static org.junit.Assert.assertTrue;



public class ExportFeaturesObjMaskTaskTest {

	private static TestLoader loader = TestLoader.createFromMavenWorkingDir();
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	static {
		RegisterBeanFactories.registerAllPackageBeanFactories();
	}

	@Test
	public void testArbitraryParams() throws InitException, FeatureCalcException, CreateException, BeanMisconfiguredException, JobExecutionException, AnchorIOException, ExperimentExecutionException, IOException {
		
		ExportFeaturesObjMaskTask task = createTask();
		task.checkMisconfigured( RegisterBeanFactories.getDefaultInstances() );
		
		Path path = folder.getRoot().toPath();
	
		boolean successful = TaskSingleInputHelper.runTaskOnSingleInput(
			MultiInputFixture.createInput(),
			task,
			path
		);

		// Successful outcome
		assertTrue(successful);

		CompareHelper.compareOutputWithSaved(
			path,
			"expectedOutput/exportFeaturesObjMask/simple01/",
			new String[] {
				"csvAgg.csv",
				"csvAll.csv",
				"stackCollection/input.tif",
				"nrgStack/nrgStack_00.tif"
			}
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

	private static StackProvider nrgStackProvider() {
		return new StackProviderChnlProvider(
			new ChnlProviderEmpty()	
		);
	}
	
	/** creates a feature-list associated with obj-mask
	 *  
	 * @throws CreateException 
	 * */
	private static List<NamedBean<FeatureListProvider<FeatureObjMaskParams>>> objMaskFeatures( TestLoader loader ) throws CreateException {
		return FeaturesFromXmlFixture.createNamedFeatureProviders("namedObjMaskFeaturesList.xml", loader);
	}
}
