package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.kernelbridge;

import org.anchoranalysis.mpp.sgmn.transformer.StateTransformer;

/**
 * No transformation occurs as Kernel and State are the same type
 * 
 * @author FEEHANO
 *
 * @param <T>
 */
public class KernelStateBridgeIdentity<T> extends KernelStateBridge<T, T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public StateTransformer<T, T> kernelToState() {
		return stateToKernel();
	}

	@Override
	public StateTransformer<T, T> stateToKernel() {
		return (a,context)->a;
	}

}
