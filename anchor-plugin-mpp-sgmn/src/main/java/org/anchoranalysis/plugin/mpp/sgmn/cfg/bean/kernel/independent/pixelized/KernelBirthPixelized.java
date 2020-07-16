/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent.pixelized;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent.KernelBirth;

/**
 * As an example, this is like sampling WITH replacement.
 *
 * @author Owen Feehan
 */
public class KernelBirthPixelized extends KernelBirth<CfgNRGPixelized> {

    // START BEAN PROPERTIES
    @BeanField private MarkProposer markProposer;
    // END BEAN PROPERTIES

    public KernelBirthPixelized() {
        // Standard bean constructor
    }

    public KernelBirthPixelized(MarkProposer markProposer) {
        this.markProposer = markProposer;
    }

    @Override
    protected Optional<Set<Mark>> proposeNewMarks(
            CfgNRGPixelized exst, int number, KernelCalcContext context) {

        Set<Mark> out = new HashSet<>();
        for (int i = 0; i < number; i++) {
            out.add(proposeNewMark(context));
        }
        return Optional.of(out);
    }

    @Override
    protected Optional<CfgNRGPixelized> calcForNewMark(
            CfgNRGPixelized exst, Set<Mark> listMarksNew, KernelCalcContext context)
            throws KernelCalcNRGException {

        ProposerContext propContext = context.proposer();

        Optional<CfgNRGPixelized> pixelized = Optional.of(exst);

        for (Mark m : listMarksNew) {
            pixelized =
                    OptionalUtilities.flatMap(pixelized, p -> proposeAndUpdate(p, m, propContext));
        }

        return pixelized;
    }

    @Override
    public void updateAfterAccpt(
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            CfgNRGPixelized exst,
            CfgNRGPixelized accptd)
            throws UpdateMarkSetException {

        for (Mark m : getMarkNew().get()) {
            VoxelizedMarkMemo memo = accptd.getMemoForMark(m);
            exst.addToUpdatablePairList(updatableMarkSetCollection, memo);
        }
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return markProposer.isCompatibleWith(testMark);
    }

    private Optional<CfgNRGPixelized> proposeAndUpdate(
            CfgNRGPixelized exst, Mark markNew, ProposerContext propContext)
            throws KernelCalcNRGException {

        VoxelizedMarkMemo pmmMarkNew = propContext.create(markNew);

        if (!applyMarkProposer(pmmMarkNew, propContext)) {
            return Optional.empty();
        }

        return Optional.of(calcUpdatedNRG(exst, pmmMarkNew, propContext));
    }

    private boolean applyMarkProposer(VoxelizedMarkMemo pmmMarkNew, ProposerContext context)
            throws KernelCalcNRGException {
        try {
            return markProposer.propose(pmmMarkNew, context);
        } catch (ProposalAbnormalFailureException e) {
            throw new KernelCalcNRGException(
                    "Failed to propose a mark due to abnormal exception", e);
        }
    }

    private static CfgNRGPixelized calcUpdatedNRG(
            CfgNRGPixelized exst, VoxelizedMarkMemo pmmMark, ProposerContext propContext)
            throws KernelCalcNRGException {

        // We calculate a new NRG by adding our marks
        CfgNRGPixelized newNRG = exst.deepCopy();
        try {
            assert (pmmMark != null);
            newNRG.add(pmmMark, propContext.getNrgStack().getNrgStack());

        } catch (FeatureCalcException e) {
            throw new KernelCalcNRGException("Cannot add pmmMarkNew", e);
        }

        return newNRG;
    }

    public MarkProposer getMarkProposer() {
        return markProposer;
    }

    public void setMarkProposer(MarkProposer markProposer) {
        this.markProposer = markProposer;
    }

    private Mark proposeNewMark(KernelCalcContext context) {
        return context.cfgGen().getCfgGen().newTemplateMark();
    }
}
