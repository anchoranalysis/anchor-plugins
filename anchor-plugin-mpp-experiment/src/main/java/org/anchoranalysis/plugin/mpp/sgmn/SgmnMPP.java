package org.anchoranalysis.plugin.mpp.sgmn;

/*
 * #%L
 * anchor-mpp-sgmn
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
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.bean.shared.random.RandomNumberGeneratorBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.memory.MemoryUtilities;
import org.anchoranalysis.core.name.provider.INamedProvider;
import org.anchoranalysis.core.name.store.NamedProviderStore;
import org.anchoranalysis.core.name.store.SharedObjects;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.OptScheme;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.OptSchemeInitContext;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.termination.TriggerTerminationCondition;
import org.anchoranalysis.mpp.sgmn.optscheme.OptTerminatedEarlyException;

import ch.ethz.biol.cell.beaninitparams.MPPInitParams;
import ch.ethz.biol.cell.imageprocessing.io.generator.serialized.GroupParamsGenerator;
import ch.ethz.biol.cell.mpp.DualStack;
import ch.ethz.biol.cell.mpp.cfg.Cfg;
import ch.ethz.biol.cell.mpp.cfg.CfgGen;
import ch.ethz.biol.cell.mpp.feedback.FeedbackReceiverBean;
import ch.ethz.biol.cell.mpp.mark.GlobalRegionIdentifiers;
import ch.ethz.biol.cell.mpp.nrg.CfgNRG;
import ch.ethz.biol.cell.mpp.nrg.CfgNRGPixelized;
import ch.ethz.biol.cell.mpp.nrg.CfgWithNrgTotal;
import ch.ethz.biol.cell.mpp.nrg.NRGSchemeWithSharedFeatures;
import ch.ethz.biol.cell.mpp.nrg.nrgscheme.creator.NRGSchemeCreator;
import ch.ethz.biol.cell.mpp.pair.ListUpdatableMarkSetCollection;
import ch.ethz.biol.cell.sgmn.cfg.CfgSgmn;


// Segments a channel with marked pointed processes
public class SgmnMPP extends CfgSgmn {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5623335920871918625L;

	// START BEAN PROPERTIES
	@BeanField
	private OptScheme<CfgNRGPixelized,CfgNRGPixelized> optScheme = null;
	
	@BeanField
	private CfgGen cfgGen = null;
	
	@BeanField
	private NRGSchemeCreator nrgSchemeCreator = null;
	
	@BeanField
	private KernelProposer<CfgNRGPixelized> kernelProposer = null;
	
	@BeanField
	private RandomNumberGeneratorBean randomNumberGenerator = null;
	
	@BeanField
	private FeedbackReceiverBean<CfgNRGPixelized> feedbackReceiver = null;
	
	@BeanField
	private Define namedDefinitions = null;
	
	@BeanField
	private int nrgSchemeIndCacheSize = 50;
	
	// For debugging, allows us to exit before optimization begins
	@BeanField
	private boolean exitBeforeOpt = false;
	// END BEAN PROPERTIES
	
	private NRGSchemeWithSharedFeatures nrgSchemeShared;
	
	public SgmnMPP() {
		super();
	}
	
	@Override
	public ExperimentStateMPP createExperimentState() {
		return new ExperimentStateMPP(kernelProposer,namedDefinitions);
	}
	
	// Do segmentation
	@Override
	public Cfg sgmn(
		NamedImgStackCollection stackCollection,
		INamedProvider<ObjMaskCollection> objMaskCollection,
		ExperimentExecutionArguments expArgs,
		KeyValueParams params,
		LogErrorReporter logger,
		BoundOutputManagerRouteErrors outputManager
	) throws SgmnFailedException {
		
		assert(stackCollection!=null);
		TriggerTerminationCondition triggerTerminationCondition = new TriggerTerminationCondition();
		
		
		ListUpdatableMarkSetCollection updatableMarkSetCollection = new ListUpdatableMarkSetCollection();
		
		DualStack dualStack = initAndPrepareInputStackCollectionForOpt(stackCollection, objMaskCollection, updatableMarkSetCollection, params, logger, outputManager);
		
		// THIS SHOULD BE THE POINT AT WHICH WE LET PsoImage go out of scope, and everything in it, which isn't being used
		//   can be garbage collected.  This will reduce memory usage for the rest of the algorithm, where hopefully
		//   only what is needed will be kept
		MemoryUtilities.logMemoryUsage("Before findOpt (after clean up)", logger.getLogReporter() );
		
		if (exitBeforeOpt) {
			return new Cfg();
		}
		
		if (params!=null) {
			outputManager.getWriterCheckIfAllowed().write(
				"groupParams",
				() -> new GroupParamsGenerator(params)
			);
		}
		
		CfgWithNrgTotal cfgNRG = findOpt(
			dualStack,			
			updatableMarkSetCollection,
			triggerTerminationCondition,
			expArgs,
			logger,
			outputManager
		);
		return cfgNRG.getCfg().deepCopy();
		
	}
		
	private DualStack initAndPrepareInputStackCollectionForOpt(
		NamedImgStackCollection stackCollection,
		INamedProvider<ObjMaskCollection> objMaskCollection,
		ListUpdatableMarkSetCollection updatableMarkSetCollection,
		KeyValueParams params,
		LogErrorReporter logger,
		BoundOutputManagerRouteErrors outputManager
	) throws SgmnFailedException {
		
		try {
			MemoryUtilities.logMemoryUsage("Start of SgmnMPP:sgmn", logger.getLogReporter());
			
			MPPInitParams soMPP = SgmnMPPHelper.createParamsMPP(
				namedDefinitions,
				randomNumberGenerator,
				stackCollection,
				objMaskCollection,
				params,
				new SharedObjects( logger ),
				logger
			);
			init( soMPP, logger );
			
			NRGStackWithParams nrgStack = SgmnMPPHelper.createNRGStack( soMPP.getImage().getStackCollection(), params );
			SgmnMPPHelper.writeStacks( soMPP.getImage(), nrgStack, logger, outputManager );
			
			logger.getLogReporter().log("Distinct number of probMap = " + updatableMarkSetCollection.numProbMap() );
			
			// We initialise the feedback recev
			feedbackReceiver.initRecursive(soMPP, logger);
			
			MemoryUtilities.logMemoryUsage("Before findOpt (before cleanup)", logger.getLogReporter() );
		
			// There are two elements of the ProposerSharedObjects that we need to update with changes to the accepted
			//   configuration
			new UpdateMarkSet(soMPP, nrgStack, updatableMarkSetCollection, logger).apply();
			
			return wrapWithBackground(nrgStack, soMPP.getImage().getStackCollection() );

		} catch( InitException | CreateException | OperationFailedException e ) {
			throw new SgmnFailedException(e);
		}
	}
		
	private void init( MPPInitParams soMPP, LogErrorReporter logger ) throws InitException {
		cfgGen.initRecursive( logger );
		
		nrgSchemeShared = SgmnMPPHelper.initNRG( nrgSchemeCreator, nrgSchemeIndCacheSize, soMPP.getFeature(), logger );

		// The kernelProposers can change proposerSharedObjects
		SgmnMPPHelper.initKernelProposers( kernelProposer, cfgGen, soMPP, logger );	
	}
	
	// TODO integrate params with OptSchemeInitContext
	private CfgWithNrgTotal findOpt(
		DualStack dualStack,
		ListUpdatableMarkSetCollection updatableMarkSetCollection,
		TriggerTerminationCondition triggerTerminationCondition,
		ExperimentExecutionArguments expArgs,
		LogErrorReporter logger,
		BoundOutputManagerRouteErrors outputManager
	) throws SgmnFailedException {
		try {
			OptSchemeInitContext initContext = new OptSchemeInitContext(
				"MPP Sgmn",
				nrgSchemeShared,
				dualStack,
				triggerTerminationCondition,
				expArgs,
				logger,
				outputManager,
				randomNumberGenerator.create(),
				cfgGen
			);
			
			CfgNRG cfgNRG = optScheme.findOpt(
				kernelProposer,
				updatableMarkSetCollection,
				feedbackReceiver,
				initContext
			).getCfgNRG();
			
			SgmnMPPOutputter.outputResults(
				cfgNRG,
				dualStack,
				nrgSchemeShared.getNrgScheme().getRegionMap().membershipWithFlagsForIndex( GlobalRegionIdentifiers.SUBMARK_INSIDE ),
				logger,
				outputManager
			);
						
			return cfgNRG.getCfgWithTotal();
			
		} catch (OptTerminatedEarlyException e) {
			throw new SgmnFailedException("Optimization terminated early", e);
		} catch (OperationFailedException e) {
			throw new SgmnFailedException("Some operation failed", e);
		}
	}
	
	
	private DualStack wrapWithBackground( NRGStackWithParams nrgStack, NamedProviderStore<Stack> store ) throws CreateException {
		DisplayStack background = BackgroundCreator.createBackground(
			store,
			getBackgroundStackName()
		); 
		return new DualStack(nrgStack,background);
	}
		
	// START GETTERS AND SETTERS
	
	public OptScheme<CfgNRGPixelized,CfgNRGPixelized> getOptScheme() {
		return optScheme;
	}

	public void setOptScheme(OptScheme<CfgNRGPixelized,CfgNRGPixelized> optScheme) {
		this.optScheme = optScheme;
	}


	public CfgGen getCfgGen() {
		return cfgGen;
	}


	public void setCfgGen(CfgGen cfgGen) {
		this.cfgGen = cfgGen;
	}

	public KernelProposer<CfgNRGPixelized> getKernelProposer() {
		return kernelProposer;
	}


	public void setKernelProposer(KernelProposer<CfgNRGPixelized> kernelProposer) {
		this.kernelProposer = kernelProposer;
	}


	public FeedbackReceiverBean<CfgNRGPixelized> getFeedbackReceiver() {
		return feedbackReceiver;
	}

	public void setFeedbackReceiver(FeedbackReceiverBean<CfgNRGPixelized> feedbackReceiver) {
		this.feedbackReceiver = feedbackReceiver;
	}

	public Define getNamedDefinitions() {
		return namedDefinitions;
	}

	public void setNamedDefinitions(Define namedDefinitions) {
		this.namedDefinitions = namedDefinitions;
	}

	public NRGSchemeCreator getNrgSchemeCreator() {
		return nrgSchemeCreator;
	}

	public void setNrgSchemeCreator(NRGSchemeCreator nrgSchemeCreator) {
		this.nrgSchemeCreator = nrgSchemeCreator;
	}

	public int getNrgSchemeIndCacheSize() {
		return nrgSchemeIndCacheSize;
	}

	public void setNrgSchemeIndCacheSize(int nrgSchemeIndCacheSize) {
		this.nrgSchemeIndCacheSize = nrgSchemeIndCacheSize;
	}

	public boolean isExitBeforeOpt() {
		return exitBeforeOpt;
	}

	public void setExitBeforeOpt(boolean exitBeforeOpt) {
		this.exitBeforeOpt = exitBeforeOpt;
	}

	public RandomNumberGeneratorBean getRandomNumberGenerator() {
		return randomNumberGenerator;
	}

	public void setRandomNumberGenerator(RandomNumberGeneratorBean randomNumberGenerator) {
		this.randomNumberGenerator = randomNumberGenerator;
	}
	// END GETTERS AND SETTERS
	
}
