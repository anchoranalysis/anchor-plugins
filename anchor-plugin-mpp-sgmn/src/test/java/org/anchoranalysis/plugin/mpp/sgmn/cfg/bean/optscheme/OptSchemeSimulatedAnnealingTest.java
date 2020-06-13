package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.feature.bean.nrgscheme.NRGScheme;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.feature.nrg.scheme.NRGSchemeWithSharedFeatures;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.random.RandomNumberGeneratorMersenne;
import org.anchoranalysis.feature.shared.SharedFeatureMulti;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposerOption;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.termination.TriggerTerminationCondition;
import org.anchoranalysis.mpp.sgmn.optscheme.DualStack;
import org.anchoranalysis.mpp.sgmn.optscheme.OptSchemeInitContext;
import org.anchoranalysis.mpp.sgmn.optscheme.OptTerminatedEarlyException;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.FeedbackReceiver;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent.pixelized.KernelBirthPixelized;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.proposer.KernelProposerOptionSingle;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.mode.DirectAssignMode;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.termination.NumberIterations;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.image.BoundContextFixture;
import org.anchoranalysis.test.image.NRGStackFixture;
import org.junit.Test;

import ch.ethz.biol.cell.mpp.feedback.reporter.ConsoleAcceptedReporter;
import ch.ethz.biol.cell.mpp.anneal.AnnealSchemeGeom;

@SuppressWarnings("unused")
public class OptSchemeSimulatedAnnealingTest {

	@Test
	public void test() throws OptTerminatedEarlyException, CreateException, InitException {
		
		/*AnnealSchemeGeom annealScheme = new AnnealSchemeGeom(1e-20, 0.99995);
		
		OptSchemeSimulatedAnnealing<CfgNRGPixelized, CfgNRGPixelized, CfgNRGPixelized> optScheme = new OptSchemeSimulatedAnnealing<>();
		optScheme.setAnnealScheme(annealScheme);
		optScheme.setAssignMode( new DirectAssignMode<>() );
		optScheme.setTermCondition( new NumberIterations(50) );
		
		
		
		ListUpdatableMarkSetCollection updatableMarkSetCollection = new ListUpdatableMarkSetCollection();
		FeedbackReceiver<CfgNRGPixelized> feedbackReceiver = new ConsoleAcceptedReporter();
		OptSchemeInitContext initContext = new OptSchemeInitContext(
			"testExperiment",
			nrgScheme(),
			new DualStack( NRGStackFixture.create(false, false) ),
			new TriggerTerminationCondition(),
			BoundContextFixture.withSimpleLogger(),
			new RandomNumberGeneratorMersenne(true),
			new CfgGen()
		);
		optScheme.findOpt(kernelProposer(), updatableMarkSetCollection, feedbackReceiver, initContext);*/
		
		fail("Not yet implemented");
	}
	
	private static KernelProposer<CfgNRGPixelized> kernelProposer() throws InitException {
		KernelProposer<CfgNRGPixelized> kernelProposer = new KernelProposer<>();
		kernelProposer.setInitialKernel( new KernelBirthPixelized() );
		
		List<KernelProposerOption<CfgNRGPixelized>> list = new ArrayList<>();
		list.add( new KernelProposerOptionSingle<>(new KernelBirthPixelized(), 0.4) );
		kernelProposer.setOptionList(list);
		kernelProposer.init();
		return kernelProposer;
	}

	private static NRGSchemeWithSharedFeatures nrgScheme() {
		return new NRGSchemeWithSharedFeatures(
			new NRGScheme(),
			new SharedFeatureMulti(),
			10,
			LoggingFixture.simpleLogErrorReporter()
		);		
	}
}
