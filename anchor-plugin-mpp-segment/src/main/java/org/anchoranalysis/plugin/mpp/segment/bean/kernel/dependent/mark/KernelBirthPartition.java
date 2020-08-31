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
import java.util.Set;
import org.anchoranalysis.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculationContext;
import org.anchoranalysis.plugin.mpp.segment.bean.kernel.independent.KernelBirth;
import org.anchoranalysis.plugin.mpp.segment.optscheme.MarksFromPartition;

/**
 * Proposes new marks ONLY if they haven't already been proposed and accepted.
 *
 * <p>As an example, this is like sampling WITHOUT replacement.
 *
 * @author Owen Feehan
 */
public class KernelBirthPartition extends KernelBirth<MarksFromPartition> {

    @Override
    protected Optional<Set<Mark>> proposeNewMarks(
            MarksFromPartition exst, int number, KernelCalculationContext context) {
        assert (exst != null);
        assert (exst.getPartition() != null);
        return exst.getPartition().sampleFromAvailable(number);
    }

    @Override
    protected Optional<MarksFromPartition> calculateForNewMark(
            MarksFromPartition exst, Set<Mark> listMarksNew, KernelCalculationContext context)
            throws KernelCalculateEnergyException {

        if (listMarksNew == null || listMarksNew.isEmpty()) {
            return Optional.empty();
        }

        MarkCollection marks = exst.getMarks();
        for (Mark mark : listMarksNew) {
            marks = calculateUpdatedMarks(marks, mark);
        }

        return Optional.of(exst.copyChange(marks));
    }

    @Override
    public void updateAfterAcceptance(
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            MarksFromPartition energyExisting,
            MarksFromPartition energyNew)
            throws UpdateMarkSetException {
        marksNew().ifPresent(marks -> energyNew.getPartition().moveAvailableToAccepted(marks));
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }

    private static MarkCollection calculateUpdatedMarks(MarkCollection exst, Mark mark) {
        MarkCollection newMarks = exst.shallowCopy();
        newMarks.add(mark);
        return newMarks;
    }
}
