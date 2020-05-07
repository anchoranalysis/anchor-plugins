package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.bean.nrgscheme.NRGSchemeCreator;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRG;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgWithNrgTotal;
import org.anchoranalysis.anchor.mpp.feature.nrg.scheme.NRGSchemeWithSharedFeatures;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;

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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.memory.MemoryUtilities;
import org.anchoranalysis.core.name.provider.INamedProvider;
import org.anchoranalysis.core.name.store.NamedProviderStore;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.core.random.RandomNumberGeneratorMersenne;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.mpp.io.input.MPPInitParamsFactory;
import org.anchoranalysis.mpp.io.output.BackgroundCreator;
import org.anchoranalysis.mpp.sgmn.bean.cfg.CfgSgmn;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.OptScheme;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.termination.TriggerTerminationCondition;
import org.anchoranalysis.mpp.sgmn.optscheme.DualStack;
import org.anchoranalysis.mpp.sgmn.optscheme.OptSchemeInitContext;
import org.anchoranalysis.mpp.sgmn.optscheme.OptTerminatedEarlyException;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.SgmnMPPState;


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
	private FeedbackReceiverBean<CfgNRGPixelized> feedbackReceiver = null;
	
	@BeanField
	private Define define = null;
	
	@BeanField
	private int nrgSchemeIndCacheSize = 50;
	
	// For debugging, allows us to exit before optimization begins
	@BeanField
	private boolean exitBeforeOpt = false;
	
	/** If TRUE uses a constant seed for the random-number-generator (useful for debugging) otherwise seeds with system clock */
	@BeanField
	private boolean fixRandomSeed = false;
	// END BEAN PROPERTIES
	
	private NRGSchemeWithSharedFeatures nrgSchemeShared;
	
	public SgmnMPP() {
		super();
	}
	
	@Override
	public SgmnMPPState createExperimentState() {
		return new SgmnMPPState(kernelProposer,define);
	}
	
	// Do segmentation
	@Override
	public Cfg sgmn(
		NamedImgStackCollection stackCollection,
		INamedProvider<ObjMaskCollection> objMaskCollection,
		Optional<KeyValueParams> keyValueParams,
		BoundIOContext context
	) throws SgmnFailedException {
		
		assert(stackCollection!=null);
	
		ListUpdatableMarkSetCollection updatableMarkSetCollection = new ListUpdatableMarkSetCollection();
		
		DualStack dualStack = initAndPrepareInputStackCollectionForOpt(
			stackCollection,
			objMaskCollection,
			updatableMarkSetCollection,
			keyValueParams,
			context
		);
		
		// THIS SHOULD BE THE POINT AT WHICH WE LET PsoImage go out of scope, and everything in it, which isn't being used
		//   can be garbage collected.  This will reduce memory usage for the rest of the algorithm, where hopefully
		//   only what is needed will be kept
		MemoryUtilities.logMemoryUsage("Before findOpt (after clean up)", context.getLogger().getLogReporter() );
		
		if (exitBeforeOpt) {
			return new Cfg();
		}
		
		if (keyValueParams.isPresent()) {
			context.getOutputManager().getWriterCheckIfAllowed().write(
				"groupParams",
				() -> new GroupParamsGenerator(keyValueParams.get())
			);
		}
				
		OptSchemeInitContext initContext = new OptSchemeInitContext(
			"MPP Sgmn",
			nrgSchemeShared,
			dualStack,
			new TriggerTerminationCondition(),
			context,
			new RandomNumberGeneratorMersenne(fixRandomSeed),
			cfgGen
		);
		
		CfgWithNrgTotal cfgNRG = findOpt(dualStack,	updatableMarkSetCollection,	initContext);
		return cfgNRG.getCfg().deepCopy();
		
	}
		
	private DualStack initAndPrepareInputStackCollectionForOpt(
		NamedImgStackCollection stackCollection,
		INamedProvider<ObjMaskCollection> objMaskCollection,
		ListUpdatableMarkSetCollection updatableMarkSetCollection,
		Optional<KeyValueParams> keyValueParams,
		BoundIOContext context
	) throws SgmnFailedException {
		
		try {
			MemoryUtilities.logMemoryUsage("Start of SgmnMPP:sgmn", context.getLogReporter() );
						
			MPPInitParams soMPP = MPPInitParamsFactory.createFromExistingCollections(
				context,
				Optional.ofNullable(define),
				Optional.of(stackCollection),
				Optional.of(objMaskCollection),
				keyValueParams
			);
			
			init( soMPP, context.getLogger() );
			
			NRGStackWithParams nrgStack = SgmnMPPHelper.createNRGStack(
				soMPP.getImage().getStackCollection(),
				keyValueParams.orElse( new KeyValueParams() )
			);
			SgmnMPPHelper.writeStacks( soMPP.getImage(), nrgStack, context );
			
			context.getLogReporter().log("Distinct number of probMap = " + updatableMarkSetCollection.numProbMap() );
			
			// We initialise the feedback recev
			feedbackReceiver.initRecursive(soMPP, context.getLogger());
			
			MemoryUtilities.logMemoryUsage("Before findOpt (before cleanup)", context.getLogReporter());
		
			// There are two elements of the ProposerSharedObjects that we need to update with changes to the accepted
			//   configuration
			new UpdateMarkSet(
				soMPP,
				nrgStack,
				updatableMarkSetCollection,
				context.getLogger()
			).apply();
			
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
		OptSchemeInitContext initContext
	) throws SgmnFailedException {
		try {
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
				initContext.getLogger(),
				initContext.getOutputManager()
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

	public boolean isFixRandomSeed() {
		return fixRandomSeed;
	}

	public void setFixRandomSeed(boolean fixRandomSeed) {
		this.fixRandomSeed = fixRandomSeed;
	}

	public Define getDefine() {
		return define;
	}

	public void setDefine(Define define) {
		this.define = define;
	}
}
