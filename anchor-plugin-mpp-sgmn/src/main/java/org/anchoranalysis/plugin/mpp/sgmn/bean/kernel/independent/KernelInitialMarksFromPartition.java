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

package org.anchoranalysis.plugin.mpp.sgmn.bean.kernel.independent;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.mpp.bean.proposer.MarkCollectionProposer;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.mpp.segment.bean.kernel.KernelIndependent;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculationContext;
import org.anchoranalysis.plugin.mpp.sgmn.bean.marks.weight.ConstantWeight;
import org.anchoranalysis.plugin.mpp.sgmn.bean.marks.weight.ExtractWeightFromMark;
import org.anchoranalysis.plugin.mpp.sgmn.optscheme.MarksFromPartition;
import org.anchoranalysis.plugin.mpp.sgmn.optscheme.PartitionedMarks;

public class KernelInitialMarksFromPartition extends KernelIndependent<MarksFromPartition> {

    // START BEAN LIST
    @BeanField @Getter @Setter private MarkCollectionProposer marksProposer;

    @BeanField @Getter @Setter private ExtractWeightFromMark extractWeight = new ConstantWeight();
    // END BEAN LIST

    private MarkCollection lastMarks;

    @Override
    public Optional<MarksFromPartition> makeProposal(
            Optional<MarksFromPartition> existing, KernelCalculationContext context)
            throws KernelCalculateEnergyException {

        Optional<MarkCollection> marks = InitMarksHelper.propose(marksProposer, context);

        if (!marks.isPresent()) {
            return Optional.empty();
        }

        this.lastMarks = marks.get();

        return Optional.of(
                new MarksFromPartition(new MarkCollection(), createPartition(marks.get())));
    }

    @Override
    public double calculateAcceptanceProbability(
            int existingSize,
            int proposalSize,
            double poissonIntensity,
            Dimensions dimensions,
            double densityRatio) {
        // We always accept
        return 1;
    }

    @Override
    public String describeLast() {
        return String.format("initialMarksWithPartition(size=%d)", this.lastMarks.size());
    }

    @Override
    public void updateAfterAcceptance(
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            MarksFromPartition exst,
            MarksFromPartition accptd)
            throws UpdateMarkSetException {
        // NOTHING TO DO
    }

    @Override
    public int[] changedMarkIDArray() {
        return this.lastMarks.createIdArr();
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return marksProposer.isCompatibleWith(testMark);
    }

    private PartitionedMarks createPartition(MarkCollection marks) {
        return new PartitionedMarks(marks, mark -> extractWeight.weightFor(mark));
    }
}
