/* (C)2020 */
package ch.ethz.biol.cell.mpp.cfg.proposer;

import cern.jet.random.Poisson;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.proposer.CfgProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;

public class CfgProposerSimple extends CfgProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MarkProposer markProposer;

    @BeanField @Getter @Setter
    private int numMarks = -1; // if positive, then we use this as the definitive number of marks
    // END BEAN PROPERTIES

    // Generates a random configuration
    //   * Number of objects is decided by a Poisson distribution
    //	 * The location and attributes of marks are uniformly distributed
    @Override
    public Optional<Cfg> propose(CfgGen cfgGen, ProposerContext context)
            throws ProposalAbnormalFailureException {
        return genInitRndCfgForNumPts(generateNumberPoints(cfgGen, context), cfgGen, context);
    }

    private int generateNumberPoints(CfgGen cfgGen, ProposerContext context) {
        if (numMarks > 0) {
            return numMarks;
        } else {
            Poisson distribution =
                    context.getRandomNumberGenerator()
                            .generatePoisson(
                                    cfgGen.getReferencePoissonIntensity()
                                            * context.getDimensions().getVolume());
            return distribution.nextInt();
        }
    }

    // Generates a random configuration for a given number of points
    //	 * The location and attributes of marks are uniformly distributed
    private Optional<Cfg> genInitRndCfgForNumPts(
            int numPoints, CfgGen cfgGen, ProposerContext context)
            throws ProposalAbnormalFailureException {

        Cfg cfg = new Cfg();

        for (int i = 0; i < numPoints; i++) {
            Mark mark = cfgGen.newTemplateMark();

            VoxelizedMarkMemo pmm = context.create(mark);

            // If the proposal fails, we don't bother trying another
            if (markProposer.propose(pmm, context)) {
                cfg.add(mark);
            }
        }

        return Optional.of(cfg);
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return this.markProposer.isCompatibleWith(testMark);
    }

    @Override
    public String getBeanDscr() {
        return getBeanName();
    }
}
