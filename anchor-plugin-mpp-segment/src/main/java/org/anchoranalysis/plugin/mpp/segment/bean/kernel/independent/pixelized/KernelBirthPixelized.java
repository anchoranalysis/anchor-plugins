/*-
 * #%L
 * anchor-plugin-mpp-sgmn
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.mpp.segment.bean.kernel.independent.pixelized;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.feature.calculate.NamedFeatureCalculateException;
import org.anchoranalysis.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.mpp.proposer.ProposerContext;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculationContext;
import org.anchoranalysis.plugin.mpp.segment.bean.kernel.independent.KernelBirth;

/**
 * As an example, this is like sampling WITH replacement.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
@AllArgsConstructor
public class KernelBirthPixelized extends KernelBirth<VoxelizedMarksWithEnergy> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MarkProposer markProposer;
    // END BEAN PROPERTIES

    @Override
    protected Optional<Set<Mark>> proposeNewMarks(
            VoxelizedMarksWithEnergy exst, int number, KernelCalculationContext context) {

        Set<Mark> out = new HashSet<>();
        for (int i = 0; i < number; i++) {
            out.add(proposeNewMark(context));
        }
        return Optional.of(out);
    }

    @Override
    protected Optional<VoxelizedMarksWithEnergy> calculateForNewMark(
            VoxelizedMarksWithEnergy exst, Set<Mark> listMarksNew, KernelCalculationContext context)
            throws KernelCalculateEnergyException {

        ProposerContext propContext = context.proposer();

        Optional<VoxelizedMarksWithEnergy> pixelized = Optional.of(exst);

        for (Mark m : listMarksNew) {
            pixelized =
                    OptionalUtilities.flatMap(pixelized, p -> proposeAndUpdate(p, m, propContext));
        }

        return pixelized;
    }

    @Override
    public void updateAfterAcceptance(
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            VoxelizedMarksWithEnergy exst,
            VoxelizedMarksWithEnergy accepted)
            throws UpdateMarkSetException {

        for (Mark mark : marksNew().get()) { // NOSONAR
            VoxelizedMarkMemo memo = accepted.getMemoForMark(mark);
            exst.addToUpdatablePairList(updatableMarkSetCollection, memo);
        }
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return markProposer.isCompatibleWith(testMark);
    }

    private Optional<VoxelizedMarksWithEnergy> proposeAndUpdate(
            VoxelizedMarksWithEnergy exst, Mark markNew, ProposerContext propContext)
            throws KernelCalculateEnergyException {

        VoxelizedMarkMemo pmmMarkNew = propContext.create(markNew);

        if (!applyMarkProposer(pmmMarkNew, propContext)) {
            return Optional.empty();
        }

        return Optional.of(calculateUpdatedEnergy(exst, pmmMarkNew, propContext));
    }

    private boolean applyMarkProposer(VoxelizedMarkMemo pmmMarkNew, ProposerContext context)
            throws KernelCalculateEnergyException {
        try {
            return markProposer.propose(pmmMarkNew, context);
        } catch (ProposalAbnormalFailureException e) {
            throw new KernelCalculateEnergyException(
                    "Failed to propose a mark due to abnormal exception", e);
        }
    }

    private static VoxelizedMarksWithEnergy calculateUpdatedEnergy(
            VoxelizedMarksWithEnergy exst, VoxelizedMarkMemo pmmMark, ProposerContext propContext)
            throws KernelCalculateEnergyException {

        // We calculate a new Energy by adding our marks
        VoxelizedMarksWithEnergy newEnergy = exst.deepCopy();
        try {
            assert (pmmMark != null);
            newEnergy.add(pmmMark, propContext.getEnergyStack().getEnergyStack());

        } catch (NamedFeatureCalculateException e) {
            throw new KernelCalculateEnergyException("Cannot add pmmMarkNew", e);
        }

        return newEnergy;
    }

    private Mark proposeNewMark(KernelCalculationContext context) {
        return context.getMarkFactory().newTemplateMark();
    }
}
