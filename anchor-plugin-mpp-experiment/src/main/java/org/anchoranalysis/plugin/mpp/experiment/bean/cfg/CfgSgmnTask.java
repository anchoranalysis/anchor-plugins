package org.anchoranalysis.plugin.mpp.experiment.bean.cfg;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.cfg.Cfg;
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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.name.store.LazyEvaluationStore;
import org.anchoranalysis.core.name.store.NamedProviderStore;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.stack.wrap.WrapStackAsTimeSequenceStore;
import org.anchoranalysis.io.generator.serialized.XStreamGenerator;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.io.output.BackgroundCreator;
import org.anchoranalysis.mpp.sgmn.bean.cfg.CfgSgmn;
import org.anchoranalysis.mpp.sgmn.bean.cfg.ExperimentState;

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
	public void doJobOnInputObject(	ParametersBound<MultiInput,ExperimentState> params)	throws JobExecutionException {

		LogErrorReporter logErrorReporter = params.getLogger();
		MultiInput inputObject = params.getInputObject();
		
		assert(logErrorReporter!=null);
		
		try {
			NamedImgStackCollection stackCollection = stacksFromInput(inputObject);
			
			NamedProviderStore<ObjMaskCollection> objs = objsFromInput(inputObject, logErrorReporter);
					
			Optional<KeyValueParams> keyValueParams = keyValueParamsFromInput(inputObject, logErrorReporter);
			
			Cfg cfg = sgmn.duplicateBean().sgmn(
				stackCollection,
				objs,
				keyValueParams,
				params.context()
			);
			writeVisualization(cfg, params.getOutputManager(), stackCollection, logErrorReporter);
			
		} catch (SgmnFailedException e) {
			throw new JobExecutionException("An error occurred segmenting a configuration", e);
		} catch (BeanDuplicateException e) {
			throw new JobExecutionException("An error occurred duplicating the sgmn bean", e);
		} catch (OperationFailedException e) {
			throw new JobExecutionException(e);
		}
	}
	
	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(MultiInput.class);
	}
	
	private NamedImgStackCollection stacksFromInput( MultiInput inputObject ) throws OperationFailedException {
		NamedImgStackCollection stackCollection = new NamedImgStackCollection();
		inputObject.stack().addToStore(
			new WrapStackAsTimeSequenceStore(stackCollection)
		);
		return stackCollection;
	}
	
	private Optional<KeyValueParams> keyValueParamsFromInput( MultiInput inputObject, LogErrorReporter logErrorReporter ) throws JobExecutionException {
		NamedProviderStore<KeyValueParams> paramsCollection = new LazyEvaluationStore<>(logErrorReporter, "keyValueParams"); 
		try {
			inputObject.keyValueParams().addToStore(paramsCollection);
		} catch (OperationFailedException e1) {
			throw new JobExecutionException("Cannot retrieve key-value-params from input-object");
		}
		
		// We select a particular key value params to send as output
		try {
			if (!keyValueParamsID.isEmpty()) {
				return Optional.of(
					paramsCollection.getException(keyValueParamsID)
				);
			} else {
				return Optional.empty();
			}
			
		} catch (NamedProviderGetException e) {
			throw new JobExecutionException("Cannot retrieve key-values-params", e.summarize());
		}
	}
	
	
	private NamedProviderStore<ObjMaskCollection> objsFromInput( MultiInput inputObject, LogErrorReporter logErrorReporter ) throws OperationFailedException {
		NamedProviderStore<ObjMaskCollection> objMaskCollectionStore = new LazyEvaluationStore<>(logErrorReporter, "objMaskCollection");
		inputObject.objs().addToStore(objMaskCollectionStore);
		return objMaskCollectionStore;
	}
	
	private void writeVisualization( Cfg cfg, BoundOutputManagerRouteErrors outputManager, NamedImgStackCollection stackCollection, LogErrorReporter logErrorReporter ) {
		outputManager.getWriterCheckIfAllowed().write(
			"cfg",
			() -> new XStreamGenerator<Object>(cfg, "cfg")
		);
		
		try {
			DisplayStack backgroundStack = BackgroundCreator.createBackground(
				stackCollection,
				sgmn.getBackgroundStackName()
			);
			
			CfgVisualization.write(cfg, outputManager, backgroundStack);
		} catch (OperationFailedException | CreateException e) {
			logErrorReporter.getErrorReporter().recordError(CfgSgmnTask.class, e);
		}
	}
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}
		
	@Override
	public ExperimentState beforeAnyJobIsExecuted(BoundOutputManagerRouteErrors outputManager, ParametersExperiment params) throws ExperimentExecutionException {
		ExperimentState es = sgmn.createExperimentState();
		es.outputBeforeAnyTasksAreExecuted(outputManager);
		return es;
	}

	@Override
	public void afterAllJobsAreExecuted(ExperimentState sharedState, BoundIOContext context) throws ExperimentExecutionException {
		sharedState.outputAfterAllTasksAreExecuted(
			context.getOutputManager()
		);
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
