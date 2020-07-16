/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.kernel.assigner;

import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.mpp.sgmn.kernel.KernelAssigner;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.mpp.sgmn.kernel.proposer.KernelWithID;
import org.anchoranalysis.mpp.sgmn.optscheme.step.OptimizationStep;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;

public class KernelAssignerAddErrorLevel<S, T> implements KernelAssigner<S, T> {

    private KernelAssigner<S, T> kernelAssigner;
    private ErrorNode parentErrorNode;

    public KernelAssignerAddErrorLevel(
            KernelAssigner<S, T> kernelAssigner, ErrorNode parentErrorNode) {
        super();
        this.kernelAssigner = kernelAssigner;
        this.parentErrorNode = parentErrorNode;
    }

    @Override
    public void assignProposal(
            OptimizationStep<S, T> optStep, TransformationContext context, KernelWithID<S> kid)
            throws KernelCalcNRGException {

        // Add a sub-level with the Kernel name in the proposer-failure-description
        ErrorNode errorNode = parentErrorNode.add(kid.getKernel().getName());

        // We assign the proposal CfgNRG
        kernelAssigner.assignProposal(optStep, context.replaceError(errorNode), kid);
    }
}
