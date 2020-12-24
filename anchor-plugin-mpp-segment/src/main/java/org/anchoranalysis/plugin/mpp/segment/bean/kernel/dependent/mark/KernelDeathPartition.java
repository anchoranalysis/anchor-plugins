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

package org.anchoranalysis.plugin.mpp.segment.bean.kernel.dependent.mark;

import java.util.Optional;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.mpp.feature.mark.UpdatableMarksList;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.mpp.proposer.ProposerContext;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.plugin.mpp.segment.bean.kernel.independent.KernelDeath;
import org.anchoranalysis.plugin.mpp.segment.optimization.MarksFromPartition;

public class KernelDeathPartition extends KernelDeath<MarksFromPartition> {

    @Override
    protected Optional<MarkAnd<Mark, MarksFromPartition>> removeAndUpdateEnergy(
            MarksFromPartition existing, ProposerContext context)
            throws KernelCalculateEnergyException {

        int index = selectIndexToRmv(existing.getMarks(), context);

        if (index == -1) {
            return Optional.empty();
        }

        MarkCollection marksNew = existing.getMarks().shallowCopy();
        marksNew.remove(index);
        return Optional.of(
                new MarkAnd<>(existing.getMarks().get(index), existing.copyChange(marksNew)));
    }

    @Override
    public void updateAfterAcceptance(
            UpdatableMarksList updatableMarkSetCollection,
            MarksFromPartition energyExisting,
            MarksFromPartition energyNew)
            throws UpdateMarkSetException {
        OptionalUtilities.ifPresent(
                getMarkRmv(), energyNew.getPartition()::moveAcceptedToAvailable);
    }
}
