package ch.ethz.biol.cell.countchrom.experiment;

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.overlay.Overlay;
import org.anchoranalysis.bean.annotation.AllowEmpty;

/*
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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



import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.idgetter.IDGetterIter;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.core.name.store.LazyEvaluationStore;
import org.anchoranalysis.core.name.store.NamedProviderStore;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.ParametersBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.stack.wrap.WrapStackAsTimeSequenceStore;
import org.anchoranalysis.io.bean.objmask.writer.RGBOutlineWriter;
import org.anchoranalysis.io.bean.objmask.writer.RGBSolidWriter;
import org.anchoranalysis.io.generator.serialized.XStreamGenerator;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.mpp.sgmn.BackgroundCreator;

import ch.ethz.biol.cell.imageprocessing.io.generator.raster.CfgGenerator;
import ch.ethz.biol.cell.imageprocessing.io.generator.raster.ColoredCfgWithDisplayStack;
import ch.ethz.biol.cell.mpp.cfg.Cfg;
import ch.ethz.biol.cell.mpp.gui.videostats.internalframe.markredraw.ColoredCfg;
import ch.ethz.biol.cell.sgmn.cfg.CfgSgmn;
import ch.ethz.biol.cell.sgmn.cfg.ExperimentState;

public class CfgSgmnTask extends Task<MultiInput,ExperimentState>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5792374230960475316L;
	
	// START BEAN PROPERTIES
	@BeanField
	private CfgSgmn sgmn = null;
	
	@BeanField
	private String outputNameOriginal = "original";
	
	@BeanField @AllowEmpty
	private String keyValueParamsID = "";
	// END BEAN PROPERTIES
	
	public CfgSgmnTask() {
		super();
	}
	
	@Override
	protected void doJobOnInputObject(	ParametersBound<MultiInput,ExperimentState> params)	throws JobExecutionException {

		LogErrorReporter logErrorReporter = params.getLogErrorReporter();
		MultiInput inputObject = params.getInputObject();
		BoundOutputManagerRouteErrors outputManager = params.getOutputManager();
		
		assert(logErrorReporter!=null);
		
		try {
			CfgSgmn is = sgmn.duplicateBean();
		
			//NamedChnlCollectionForSeries ncc = inputObject.createForSeries(0, progressReporter);
			
			NamedImgStackCollection stackCollection = new NamedImgStackCollection();
			inputObject.stack().addToStore(
				new WrapStackAsTimeSequenceStore(stackCollection)
			);
			
			NamedProviderStore<KeyValueParams> paramsCollection = new LazyEvaluationStore<>(logErrorReporter, "keyValueParams"); 
			inputObject.keyValueParams().addToStore(paramsCollection);
			
			// We select a particular key value params to send as output
			KeyValueParams paramsKeyValue = (!keyValueParamsID.isEmpty()) ? paramsCollection.getException(keyValueParamsID) : null;
			
			
			NamedProviderStore<ObjMaskCollection> objMaskCollectionStore = new LazyEvaluationStore<>(logErrorReporter, "objMaskCollection");
			inputObject.objs().addToStore(objMaskCollectionStore);
			
			Cfg cfg = is.sgmn( stackCollection, objMaskCollectionStore, params.getExperimentArguments(), paramsKeyValue, logErrorReporter, params.getOutputManager() );
	
			outputManager.getWriterCheckIfAllowed().write(
				"cfg",
				() -> new XStreamGenerator<Object>(cfg, "cfg")
			);

			
			{
				DisplayStack backgroundStack = BackgroundCreator.createBackground(
					stackCollection,
					sgmn.getBackgroundStackName()
				);
				writeCfgVisualizations(cfg, outputManager, backgroundStack);
			}
			
		} catch (SgmnFailedException | OperationFailedException | GetOperationFailedException | BeanDuplicateException | CreateException e) {
			throw new JobExecutionException(e);
		}
	}
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}
	
	private void writeCfgVisualizations( Cfg cfg, BoundOutputManagerRouteErrors outputManager, DisplayStack backgroundStack ) throws OperationFailedException {
		ColorIndex colorIndex = outputManager.getOutputWriteSettings().genDefaultColorIndex(cfg.size());
		
		ColoredCfg coloredCfg = new ColoredCfg(cfg,colorIndex,new IDGetterIter<Mark>());
		
		//outputManager.write( "config", new CfgNRGGenerator(cfgNRG, chromSgmn.getNrgScheme(), stackChnlChrom, null, colorIndex, new IDGetterIter<Mark>()) );
		outputManager.getWriterCheckIfAllowed().write(
			"solid",
			() -> new CfgGenerator(
				new RGBSolidWriter(),
				new ColoredCfgWithDisplayStack( coloredCfg, backgroundStack),
				new IDGetterIter<Overlay>()
			)
		);
		outputManager.getWriterCheckIfAllowed().write(
			"outline",
			() -> new CfgGenerator(
				new RGBOutlineWriter(),
				new ColoredCfgWithDisplayStack( coloredCfg, backgroundStack),
				new IDGetterIter<Overlay>()
			)
		);
	}
	
	@Override
	public ExperimentState beforeAnyJobIsExecuted(BoundOutputManagerRouteErrors outputManager, ParametersExperiment params) throws ExperimentExecutionException {
		ExperimentState es = sgmn.createExperimentState();
		es.outputBeforeAnyTasksAreExecuted(outputManager);
		return es;
	}

	@Override
	public void afterAllJobsAreExecuted(BoundOutputManagerRouteErrors outputManager, ExperimentState sharedState, LogReporter logReporter) throws ExperimentExecutionException {
		sharedState.outputAfterAllTasksAreExecuted(outputManager);
		
	}

	public CfgSgmn getSgmn() {
		return sgmn;
	}

	public void setSgmn(CfgSgmn sgmn) {
		this.sgmn = sgmn;
	}

	public String getKeyValueParamsID() {
		return keyValueParamsID;
	}

	public void setKeyValueParamsID(String keyValueParamsID) {
		this.keyValueParamsID = keyValueParamsID;
	}


}
