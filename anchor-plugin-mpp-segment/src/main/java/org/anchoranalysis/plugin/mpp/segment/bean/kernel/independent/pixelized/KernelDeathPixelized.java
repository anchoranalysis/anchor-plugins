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

import java.util.Optional;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.feature.calculate.NamedFeatureCalculateException;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.feature.mark.UpdatableMarksList;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.UpdateMarkSetException;
import org.anchoranalysis.mpp.proposer.ProposerContext;
import org.anchoranalysis.mpp.segment.optimization.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.plugin.mpp.segment.bean.kernel.independent.KernelDeath;

public class KernelDeathPixelized extends KernelDeath<VoxelizedMarksWithEnergy> {

    @Override
    protected Optional<MarkAnd<Mark, VoxelizedMarksWithEnergy>> removeAndUpdateEnergy(
            VoxelizedMarksWithEnergy exst, ProposerContext context)
            throws KernelCalculateEnergyException {
        return removeMarkAndUpdateEnergy(exst, context);
    }

    @Override
    public void updateAfterAcceptance(
            UpdatableMarksList updatableMarkSetCollection,
            VoxelizedMarksWithEnergy energyExisting,
            VoxelizedMarksWithEnergy energyNew)
            throws UpdateMarkSetException {
        OptionalUtilities.ifPresent(
                getMarkRmv(),
                mark -> energyExisting.rmvFromUpdatablePairList(updatableMarkSetCollection, mark));
    }

    private static Optional<MarkAnd<Mark, VoxelizedMarksWithEnergy>> removeMarkAndUpdateEnergy(
            VoxelizedMarksWithEnergy exst, ProposerContext propContext)
            throws KernelCalculateEnergyException {

        int index = selectIndexToRmv(exst.getMarks().getMarks(), propContext);

        if (index == -1) {
            return Optional.empty();
        }

        return Optional.of(
                new MarkAnd<>(
                        exst.getMemoForIndex(index).getMark(),
                        updatedEnergyAfterRemoval(index, exst, propContext)));
    }

    private static VoxelizedMarksWithEnergy updatedEnergyAfterRemoval(
            int index, VoxelizedMarksWithEnergy exst, ProposerContext propContext)
            throws KernelCalculateEnergyException {
        // We calculate a new Energy by exchanging our marks
        VoxelizedMarksWithEnergy newEnergy = exst.shallowCopy();

        try {
            newEnergy.remove(index, propContext.getEnergyStack().withoutParams());
        } catch (NamedFeatureCalculateException e) {
            throw new KernelCalculateEnergyException(
                    String.format("Cannot remove index %d", index), e);
        }

        return newEnergy;
    }
}
