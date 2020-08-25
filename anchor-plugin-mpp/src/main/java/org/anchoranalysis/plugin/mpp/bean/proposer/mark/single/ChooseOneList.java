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

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.mpp.proposer.ProposerContext;
import org.anchoranalysis.mpp.proposer.visualization.CreateProposalVisualization;

// Chooses one from a list
public class ChooseOneList extends MarkProposerFromList {

    private int lastIndex = -1;

    @Override
    protected boolean propose(
            VoxelizedMarkMemo inputMark,
            ProposerContext context,
            List<MarkProposer> markProposerList)
            throws ProposalAbnormalFailureException {

        int index = context.getRandomNumberGenerator().sampleIntFromRange(markProposerList.size());

        MarkProposer markProposer = markProposerList.get(index);

        lastIndex = index;

        // Calculate position
        return markProposer.propose(inputMark, context);
    }

    @Override
    protected Optional<CreateProposalVisualization> proposalVisualization(
            boolean detailed, List<MarkProposer> markProposerList) {
        if (lastIndex != -1) {
            return markProposerList.get(lastIndex).proposalVisualization(detailed);
        }
        return Optional.empty();
    }
}
