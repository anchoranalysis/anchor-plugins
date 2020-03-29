package org.anchoranalysis.plugin.mpp.experiment.bean.seed;

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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.core.name.store.LazyEvaluationStore;
import org.anchoranalysis.core.name.store.NamedProviderStore;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.experiment.bean.seed.SeedFinder;
import org.anchoranalysis.image.experiment.identifiers.ImgStackIdentifiers;
import org.anchoranalysis.image.experiment.seed.SeedFinderException;
import org.anchoranalysis.image.io.bean.seed.SeedCollectionOutputter;
import org.anchoranalysis.image.io.generator.raster.ChnlGenerator;
import org.anchoranalysis.image.io.generator.raster.objmask.ObjMaskChnlGenerator;
import org.anchoranalysis.image.io.generator.raster.objmask.ObjMaskGenerator;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.stack.wrap.WrapStackAsTimeSequenceStore;
import org.anchoranalysis.io.bean.objmask.writer.RGBOutlineWriter;
import org.anchoranalysis.io.bean.objmask.writer.RGBSolidWriter;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.writer.WriterRouterErrors;
import org.anchoranalysis.mpp.io.input.MultiInput;



/**
 * Seed Finder Task
 *  
 * @author Owen Feehan
 *
 * @param <S> shared-state type
 */
public class SeedFinderTask<S> extends Task<MultiInput,S> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	// START BEAN PROPERTIES
	@BeanField
	private SeedFinder<S> seedFinder;
	
	@BeanField
	private String outputNameOriginal = "original";
	
	@BeanField
	private SeedCollectionOutputter seedCollectionOutputter = null;
	
	@BeanField
	private String keyValueParamsID = "";
	// END BEAN PROPERTIES
	
	@Override
	public S beforeAnyJobIsExecuted(
			BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
			throws ExperimentExecutionException {
		return seedFinder.beforeAnySeedFinding(outputManager);
	}
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}
	
	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(MultiInput.class);
	}
		
	@Override
	public void doJobOnInputObject(	ParametersBound<MultiInput,S> params ) throws JobExecutionException {
		
		LogErrorReporter logErrorReporter = params.getLogErrorReporter();
		MultiInput inputObject = params.getInputObject();
		BoundOutputManagerRouteErrors outputManager = params.getOutputManager();
		
		try {
			SeedFinder<S> seedFinderDup = seedFinder.duplicateBean();
			
			NamedImgStackCollection stackCollection = new NamedImgStackCollection();
			inputObject.stack().addToStore(
				new WrapStackAsTimeSequenceStore(stackCollection)
			);
			
			NamedProviderStore<KeyValueParams> paramsCollection = new LazyEvaluationStore<>(logErrorReporter, "keyValueParams"); 
			inputObject.keyValueParams().addToStore(paramsCollection);
			
			// We select a particular key value params to send as output
			KeyValueParams paramsKeyValue = (!keyValueParamsID.isEmpty()) ? paramsCollection.getException(keyValueParamsID) : null;
			
			// Test that values have opened correctly
			Chnl chnlIn = stackCollection.getException(ImgStackIdentifiers.INPUT_IMAGE).getChnl(0);
			
			VisualOutputter outputter = new VisualOutputter(outputManager, chnlIn);
			outputter.outputOriginalChnl(outputNameOriginal);

			NamedProviderStore<ObjMaskCollection> objMaskCollectionStore = new LazyEvaluationStore<>(logErrorReporter, "objMaskCollection");
			inputObject.objs().addToStore(objMaskCollectionStore);
			
			SeedCollection seeds = seedFinderDup.findSeeds(stackCollection, objMaskCollectionStore, params.getExperimentArguments(), paramsKeyValue, logErrorReporter, outputManager, params.getSharedState());
			seedCollectionOutputter.output(seeds, chnlIn.getDimensions().getRes(), outputManager);
			
			outputter.outputSeedsVisually( seeds );
			
		} catch (SeedFinderException | OperationFailedException | GetOperationFailedException | BeanDuplicateException e) {
			throw new JobExecutionException(e);
		}
	}
	
	private static class VisualOutputter {
		
		private BoundOutputManagerRouteErrors outputManager;
		private Chnl chnl;
		
		public VisualOutputter(BoundOutputManagerRouteErrors outputManager, Chnl chnl) {
			super();
			this.outputManager = outputManager;
			this.chnl = chnl;
		}
		
		public void outputOriginalChnl(String outputName) {
			outputManager.getWriterCheckIfAllowed().write(
					outputName,
				() -> new ChnlGenerator(chnl,"original")
			);
		}
		
		public void outputSeedsVisually( SeedCollection seeds ) {
			ObjMaskCollection objs = seeds.createMasks();
			
			WriterRouterErrors writer = outputManager.getWriterCheckIfAllowed();

			VisualObjsWriter outputter = new VisualObjsWriter(outputManager, chnl, objs);

			outputter.writeAsSubfolder(
				"maskChnl",
				writer,
				(c) -> new ObjMaskChnlGenerator(c)
			);
			
			outputter.writeAsSubfolder(
				"mask",
				writer,
				(c) -> new ObjMaskGenerator(255, c.getDimensions().getRes() )
			);
			
			outputter.writeAsImage( "outline", writer, new RGBOutlineWriter() );
			outputter.writeAsImage( "solid", writer, new RGBSolidWriter() );
		}
	}
		
	@Override
	public void afterAllJobsAreExecuted(
			BoundOutputManagerRouteErrors outputManager, S sharedState, LogReporter logReporter)
			throws ExperimentExecutionException {
		seedFinder.afterAllSeedFinding(outputManager, sharedState);
	}
	
	public SeedFinder<S> getSeedFinder() {
		return seedFinder;
	}


	public void setSeedFinder(SeedFinder<S> seedFinder) {
		this.seedFinder = seedFinder;
	}


	public SeedCollectionOutputter getSeedCollectionOutputter() {
		return seedCollectionOutputter;
	}


	public void setSeedCollectionOutputter(
			SeedCollectionOutputter seedCollectionOutputter) {
		this.seedCollectionOutputter = seedCollectionOutputter;
	}

	public String getKeyValueParamsID() {
		return keyValueParamsID;
	}


	public void setKeyValueParamsID(String keyValueParamsID) {
		this.keyValueParamsID = keyValueParamsID;
	}
}
