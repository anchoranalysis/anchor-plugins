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

package org.anchoranalysis.plugin.mpp.bean.proposer.mark.collection;

import cern.jet.random.Poisson;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.mpp.bean.mark.factory.MarkWithIdentifierFactory;
import org.anchoranalysis.mpp.bean.proposer.MarkCollectionProposer;
import org.anchoranalysis.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.mpp.proposer.ProposerContext;

public class FromMarkProposer extends MarkCollectionProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MarkProposer markProposer;

    @BeanField @Getter @Setter
    private int numMarks = -1; // if positive, then we use this as the definitive number of marks
    // END BEAN PROPERTIES

    // Generates a random configuration
    //   * Number of objects is decided by a Poisson distribution
    //	 * The location and attributes of marks are uniformly distributed
    @Override
    public Optional<MarkCollection> propose(
            MarkWithIdentifierFactory markFactory, ProposerContext context)
            throws ProposalAbnormalFailureException {
        return genInitRndMarksForNumPts(
                generateNumberPoints(markFactory, context), markFactory, context);
    }

    private int generateNumberPoints(
            MarkWithIdentifierFactory markFactory, ProposerContext context) {
        if (numMarks > 0) {
            return numMarks;
        } else {
            Poisson distribution =
                    context.getRandomNumberGenerator()
                            .generatePoisson(
                                    markFactory.getReferencePoissonIntensity()
                                            * context.dimensions().calculateVolume());
            return distribution.nextInt();
        }
    }

    // Generates a random configuration for a given number of points
    //	 * The location and attributes of marks are uniformly distributed
    private Optional<MarkCollection> genInitRndMarksForNumPts(
            int numPoints, MarkWithIdentifierFactory markFactory, ProposerContext context)
            throws ProposalAbnormalFailureException {

        MarkCollection marks = new MarkCollection();

        for (int i = 0; i < numPoints; i++) {
            Mark mark = markFactory.newTemplateMark();

            VoxelizedMarkMemo pmm = context.create(mark);

            // If the proposal fails, we don't bother trying another
            if (markProposer.propose(pmm, context)) {
                marks.add(mark);
            }
        }

        return Optional.of(marks);
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return this.markProposer.isCompatibleWith(testMark);
    }

    @Override
    public String describeBean() {
        return getBeanName();
    }
}
