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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.mpp.bean.proposer.MarkFromCollectionProposer;
import org.anchoranalysis.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.mpp.feature.error.CheckException;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.mpp.proposer.ProposerContext;

public class Check extends MarkFromCollectionProposerUnary {

    // START BEANS
    @BeanField @Getter @Setter private CheckMark checkMark;
    // END BEANS

    @Override
    public Optional<Mark> selectMarkFrom(
            MarkCollection marks, MarkFromCollectionProposer proposer, ProposerContext context)
            throws ProposalAbnormalFailureException {

        Optional<Mark> mark = proposer.selectMarkFrom(marks, context);

        if (!mark.isPresent()) {
            return mark;
        }

        try {
            checkMark.start(context.getEnergyStack());
        } catch (OperationFailedException e) {
            // Serious error
            context.getErrorNode().add(e);
            return Optional.empty();
        }

        try {
            if (checkMark.check(mark.get(), context.getRegionMap(), context.getEnergyStack())) {

                checkMark.end();

                // We have a candidate mark
                return mark;
            } else {
                checkMark.end();
                return Optional.empty();
            }
        } catch (CheckException e) {
            checkMark.end();
            throw new ProposalAbnormalFailureException(
                    "Failed to check-mark due to abnormal exception", e);
        }
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return checkMark.isCompatibleWith(testMark) && super.isCompatibleWith(testMark);
    }
}
