/*-
 * #%L
 * anchor-plugin-mpp
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

package org.anchoranalysis.plugin.mpp.bean.proposer.mark.single;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonEmpty;

public abstract class MarkProposerFromList extends MarkProposer {

    // START BEAN PROPERTIES
    @BeanField @NonEmpty @Getter @Setter private List<MarkProposer> list = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public boolean propose(VoxelizedMarkMemo inputMark, ProposerContext context)
            throws ProposalAbnormalFailureException {
        return propose(inputMark, context, list);
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {

        for (MarkProposer markProposer : list) {

            if (!markProposer.isCompatibleWith(testMark)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {
        return proposalVisualization(detailed, list);
    }

    protected abstract boolean propose(
            VoxelizedMarkMemo inputMark,
            ProposerContext context,
            List<MarkProposer> markProposerList)
            throws ProposalAbnormalFailureException;

    protected abstract Optional<CreateProposalVisualization> proposalVisualization(
            boolean detailed, List<MarkProposer> markProposerList);
}
