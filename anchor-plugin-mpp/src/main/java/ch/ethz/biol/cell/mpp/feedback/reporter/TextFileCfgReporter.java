package ch.ethz.biol.cell.mpp.feedback.reporter;

import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

import ch.ethz.biol.cell.imageprocessing.io.generator.text.ObjectAsStringGenerator;
import ch.ethz.biol.cell.mpp.cfg.Cfg;
import ch.ethz.biol.cell.mpp.feedback.OptimizationFeedbackInitParams;
import ch.ethz.biol.cell.mpp.feedback.PeriodicSubfolderReporter;
import ch.ethz.biol.cell.mpp.feedback.ReporterException;
import ch.ethz.biol.cell.mpp.nrg.CfgNRGPixelized;


public class TextFileCfgReporter extends PeriodicSubfolderReporter<Cfg> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5108453586667112608L;

	public TextFileCfgReporter() {
		super();
	}

	@Override
	public void reportBegin( OptimizationFeedbackInitParams<CfgNRGPixelized> initParams ) throws ReporterException {
		
		super.reportBegin( initParams );
		
		try {
			init( new ObjectAsStringGenerator<Cfg>() );
		} catch (OutputWriteFailedException e) {
			throw new ReporterException(e);
		}
	}
	
	@Override
	protected Cfg generateIterableElement( Reporting<CfgNRGPixelized> reporting ) {
		return reporting.getCfgNRGAfter().getCfg();
	}

	@Override
	public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {
		
	}
}
