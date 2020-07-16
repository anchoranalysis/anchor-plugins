/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRG;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public class CfgNRGSerializerPeriodicReporter extends ObjectSerializerPeriodicReporter<CfgNRG> {

    // BEAN PARAMETERS
    @BeanField @Getter @Setter private boolean proposal = false;

    /** If proposal==true, this toggles between the primary and secondary proposal */
    @BeanField @Getter @Setter private boolean secondary = false;
    // END BEAN PARAMETERS

    public CfgNRGSerializerPeriodicReporter() {
        super("cfgNRG");
    }

    @Override
    public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {
        // NOTHING TO DO
    }

    @Override
    protected Optional<CfgNRG> generateIterableElement(Reporting<CfgNRGPixelized> reporting)
            throws ReporterException {

        if (proposal) {
            return proposalCfgNRGOrNull(reporting);
        } else {
            return Optional.of(reporting.getCfgNRGAfter().getCfgNRG());
        }
    }

    private Optional<CfgNRG> proposalCfgNRGOrNull(Reporting<CfgNRGPixelized> reporting) {

        Optional<CfgNRGPixelized> p =
                secondary ? reporting.getProposalSecondary() : reporting.getProposal();
        return p.map(CfgNRGPixelized::getCfgNRG);
    }
}
