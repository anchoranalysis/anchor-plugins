/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.mpp.sgmn.kernel.proposer.KernelIterDescription;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public class KernelIterDescriptionSerializerPeriodicReporter
        extends ObjectSerializerPeriodicReporter<KernelIterDescription> {

    public KernelIterDescriptionSerializerPeriodicReporter() {
        super("kernelIterDescription");
    }

    @Override
    protected Optional<KernelIterDescription> generateIterableElement(
            Reporting<CfgNRGPixelized> reporting) {
        return Optional.of(
                new KernelIterDescription(
                        reporting.getKernel(),
                        reporting.isAccptd(),
                        reporting.getProposal().isPresent(),
                        reporting.getChangedMarkIDs(),
                        reporting.getExecutionTime(),
                        reporting.getIter(),
                        reporting.getKernelNoProposalDescription()));
    }

    @Override
    public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {
        // NOTHING TO DO
    }
}
