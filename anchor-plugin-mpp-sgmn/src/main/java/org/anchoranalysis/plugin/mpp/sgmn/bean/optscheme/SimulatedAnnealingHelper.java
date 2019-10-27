package org.anchoranalysis.plugin.mpp.sgmn.bean.optscheme;

/*-
 * #%L
 * anchor-plugin-mpp-sgmn
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.anchor.mpp.proposer.error.ProposerFailureDescription;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.termination.TerminationCondition;
import org.anchoranalysis.mpp.sgmn.cfgnrg.transformer.StateTransformer;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.mpp.sgmn.kernel.assigner.KernelAssigner;
import org.anchoranalysis.mpp.sgmn.kernel.proposer.KernelWithID;
import org.anchoranalysis.mpp.sgmn.optscheme.OptTerminatedEarlyException;
import org.anchoranalysis.mpp.sgmn.optscheme.extractscoresize.ExtractScoreSize;
import org.anchoranalysis.mpp.sgmn.optscheme.step.OptimizationStep;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;
import org.anchoranalysis.plugin.mpp.sgmn.bean.optscheme.mode.AssignMode;
import org.anchoranalysis.plugin.mpp.sgmn.kernel.assigner.KernelAssignerAddErrorLevel;
import org.anchoranalysis.plugin.mpp.sgmn.kernel.updater.KernelUpdater;
import org.anchoranalysis.plugin.mpp.sgmn.kernel.updater.KernelUpdaterSimple;
import org.anchoranalysis.plugin.mpp.sgmn.optscheme.AccptProbCalculator;
import org.anchoranalysis.plugin.mpp.sgmn.optscheme.TransformationContext;
import org.apache.commons.lang.time.StopWatch;

import ch.ethz.biol.cell.mpp.anneal.AnnealScheme;
import ch.ethz.biol.cell.mpp.pair.ListUpdatableMarkSetCollection;
import ch.ethz.biol.cell.mpp.pair.UpdateMarkSetException;

class SimulatedAnnealingHelper {
	
	
	/**
	 * Performs optimization
	 * 
	 * @param assignMode
	 * @param annealScheme
	 * @param updatableMarkSetCollection
	 * @param feedbackGenerator
	 * @param kernelProposer
	 * @param termConditionAll
	 * @param context
	 * @param S type reported back
	 * @param T optimization state
	 * @param U kernel-proposer type
	 * @return
	 * @throws OptTerminatedEarlyException
	 */
	public static <S,T,U> T doOptimization(
		AssignMode<S,T,U> assignMode,
		AnnealScheme annealScheme,
		ListUpdatableMarkSetCollection updatableMarkSetCollection,
		FeedbackGenerator<S> feedbackGenerator,
		KernelProposer<U> kernelProposer,
		TerminationCondition termConditionAll,
		TransformationContext context
	) throws OptTerminatedEarlyException {
		
		try {
			kernelProposer.initBeforeCalc(context.getKernelCalcContext());
		} catch (InitException e) {
			throw new OptTerminatedEarlyException("Init failed", e);
		}			

		OptimizationStep<U,T> optStep = new OptimizationStep<>();
		
		int iter = 0;
		do {
			optStep.setTemperature(
				annealScheme.calcTemp(iter)
			);
						
			applyKernelToOptStep(
				optStep,
				iter,
				context,
				kernelProposer,
				updatableMarkSetCollection,
				assignMode.probCalculator(annealScheme),
				assignMode.kernelAssigner(context),
				assignMode.kernelStateBridge().stateToKernel()
			);

			try {
				reportOptStep(
					optStep.reporting(
						iter,
						assignMode.stateReporter(),
						context
					),
					feedbackGenerator
				);
			} catch (OperationFailedException e) {
				throw new OptTerminatedEarlyException("Cannot create reporting for optStep", e);
			}
		}
		while( continueIterations(
			optStep.getBest(),
			iter++,			
			termConditionAll,
			assignMode.extractScoreSizeState(),
			context.getLogger().getLogReporter()
		));
		
		// We decrement the iterator to reflect its final state
		iter--;
		
		return optStep.releaseKeepBest();
	}
	
	private static <T> boolean continueIterations(
		T state,
		int iter,
		TerminationCondition termConditionAll,
		ExtractScoreSize<T> extractScoreSize,
		LogReporter logger
	) {
		return termConditionAll.continueIterations(
			iter,
			extractScoreSize.extractScore( state ),
			extractScoreSize.extractSize( state ),
			logger
		);
	}
		
	private static <S> void reportOptStep( Reporting<S> reporting, FeedbackGenerator<S> feedbackGenerator ) {
		
		if (reporting.isBest()) {
			feedbackGenerator.recordBest(reporting);
		}
		
		feedbackGenerator.record( reporting );
	}
	
		
	private static <S,T> void applyKernelToOptStep(
		OptimizationStep<S,T> optStep,
		int iter,
		TransformationContext context,
		KernelProposer<S> kernelProposer,
		ListUpdatableMarkSetCollection updatableMarkSetCollection,
		AccptProbCalculator<T> accptProbCalc,
		KernelAssigner<S,T> kernelAssigner,
		StateTransformer<T,S> funcExtractForUpdate
	) throws OptTerminatedEarlyException {
		try {
			// Propose a kernel
			KernelWithID<S> kid = proposeKernel(
				kernelProposer,
				context.getKernelCalcContext().proposer().getRe(),
				iter==0
			);
			
			KernelUpdater<S,T> kernelUpdater = new KernelUpdaterSimple<S,T>(
				updatableMarkSetCollection,
				kernelProposer.getAllKernelFactories(),
				funcExtractForUpdate
			);
	
			assignToOptStepForKernel(
				optStep,
				iter,
				kid,
				context,
				accptProbCalc,
				kernelUpdater,
				kernelAssigner
			);
			
		} catch (KernelCalcNRGException e) {
			throw new OptTerminatedEarlyException("A kernel-calculation error occurred", e);
		} catch (UpdateMarkSetException e) {
			throw new OptTerminatedEarlyException("An update-mask-set error occurred", e);
		} catch (GetOperationFailedException e) {
			throw new OptTerminatedEarlyException("A get-operation-failed", e);
		}
	}
	
	private static <S> KernelWithID<S> proposeKernel( KernelProposer<S> kernelProposer, RandomNumberGenerator re, boolean firstStep ) {
		if (firstStep) {
			return kernelProposer.initialKernel(re);
		} else {
			return kernelProposer.proposeKernel(re);
		}
	}

	private static <S,T> void assignToOptStepForKernel(
		OptimizationStep<S,T> optStep,
		int iter,
		KernelWithID<S> kid,
		TransformationContext context,
		AccptProbCalculator<T> accptProbCalc,
		KernelUpdater<S,T> kernelUpdater,
		KernelAssigner<S,T> kernelAssigner
	) throws KernelCalcNRGException, UpdateMarkSetException, GetOperationFailedException {
		
		StopWatch timer = new StopWatch();
		timer.start();
		
		// We switch off debugging for now to check out the synchronization problems
		ProposerFailureDescription error = new ProposerFailureDescription();

		// We assign the proposal CfgNRG
		createAssigner( kernelAssigner, error.getRoot() ).assignProposal(
			optStep,
			context,
			kid
		);
		
		ConsiderProposalHelper.maybeAcceptProposal(
			optStep,
			iter,
			accptProbCalc,
			kernelUpdater,
			error,
			context
		);
		
		assignExecutionTime( optStep, timer );
	}
	
	private static <S,T> KernelAssigner<S,T> createAssigner( KernelAssigner<S,T> kernelAssigner, ErrorNode error ) {
		return new KernelAssignerAddErrorLevel<S,T>( kernelAssigner, error );	
	}
		
	private static void assignExecutionTime( OptimizationStep<?,?> optStep, StopWatch timer ) {
		long time = timer.getTime();
		timer.reset();
		optStep.setExecutionTime( time );
	}

}
