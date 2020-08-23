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
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkCollectionProposer;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkCollection;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelIndependent;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalculationContext;
import org.anchoranalysis.plugin.mpp.sgmn.bean.marks.weight.ConstantWeight;
import org.anchoranalysis.plugin.mpp.sgmn.bean.marks.weight.ExtractWeightFromMark;
import org.anchoranalysis.plugin.mpp.sgmn.optscheme.MarksFromPartition;
import org.anchoranalysis.plugin.mpp.sgmn.optscheme.PartitionedMarks;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalculateEnergyException;

public class KernelInitialMarksFromPartition extends KernelIndependent<MarksFromPartition> {

    // START BEAN LIST
    @BeanField @Getter @Setter private MarkCollectionProposer cfgProposer;

    @BeanField @Getter @Setter private ExtractWeightFromMark extractWeight = new ConstantWeight();
    // END BEAN LIST

    private MarkCollection lastCfg;

    @Override
    public Optional<MarksFromPartition> makeProposal(
            Optional<MarksFromPartition> existing, KernelCalculationContext context)
            throws KernelCalculateEnergyException {

        Optional<MarkCollection> marks = InitMarksHelper.propose(cfgProposer, context);

        if (!marks.isPresent()) {
            return Optional.empty();
        }

        this.lastCfg = marks.get();

        return Optional.of(new MarksFromPartition(new MarkCollection(), createPartition(marks.get())));
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
        return String.format("initialCfgWithPartition(size=%d)", this.lastCfg.size());
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
        return this.lastCfg.createIdArr();
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return cfgProposer.isCompatibleWith(testMark);
    }

    private PartitionedMarks createPartition(MarkCollection cfg) {
        return new PartitionedMarks(cfg, mark -> extractWeight.weightFor(mark));
    }
}
