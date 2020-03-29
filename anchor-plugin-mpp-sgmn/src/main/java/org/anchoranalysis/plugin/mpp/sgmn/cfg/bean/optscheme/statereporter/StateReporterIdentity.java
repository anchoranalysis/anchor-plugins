package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.statereporter;

import org.anchoranalysis.mpp.sgmn.optscheme.StateReporter;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformer;

/**
 * Assumes the state is reported without any transformation
 * 
 * @author FEEHANO
 *
 * @param <T>
 */
public class StateReporterIdentity<T> extends StateReporter<T, T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public StateTransformer<T, T> primaryReport() {
		return (a,context)->a;
	}

	@Override
	public boolean hasSecondaryReport() {
		return false;
	}
	
	@Override
	public StateTransformer<T, T> secondaryReport() {
		return null;
	}
}
