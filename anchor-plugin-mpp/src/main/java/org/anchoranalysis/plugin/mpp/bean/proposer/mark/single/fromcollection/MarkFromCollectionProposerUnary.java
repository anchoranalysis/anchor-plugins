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
package org.anchoranalysis.plugin.mpp.bean.proposer.mark.single.fromcollection;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.mpp.bean.proposer.MarkFromCollectionProposer;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.mpp.proposer.ProposerContext;

public abstract class MarkFromCollectionProposerUnary extends MarkFromCollectionProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MarkFromCollectionProposer proposer;
    // END BEAN PROPERTIES

    @Override
    public Optional<Mark> selectMarkFrom(MarkCollection marks, ProposerContext context)
            throws ProposalAbnormalFailureException {
        return selectMarkFrom(marks, proposer, context);
    }

    protected abstract Optional<Mark> selectMarkFrom(
            MarkCollection marks, MarkFromCollectionProposer proposer, ProposerContext context)
            throws ProposalAbnormalFailureException;

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return proposer.isCompatibleWith(testMark);
    }
}
