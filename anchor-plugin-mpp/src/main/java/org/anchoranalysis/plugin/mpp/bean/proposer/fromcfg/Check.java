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
/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.fromcfg;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkFromCfgProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;

public class Check extends MarkFromCfgProposer {

    // START BEANS
    @BeanField private MarkFromCfgProposer markFromCfgProposer;

    @BeanField private CheckMark checkMark;
    // END BEANS

    @Override
    public Optional<Mark> markFromCfg(Cfg cfg, ProposerContext context)
            throws ProposalAbnormalFailureException {

        Optional<Mark> mark = markFromCfgProposer.markFromCfg(cfg, context);

        if (!mark.isPresent()) {
            return mark;
        }

        try {
            checkMark.start(context.getNrgStack());
        } catch (OperationFailedException e) {
            // Serious error
            context.getErrorNode().add(e);
            return Optional.empty();
        }

        try {
            if (checkMark.check(mark.get(), context.getRegionMap(), context.getNrgStack())) {

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
        return checkMark.isCompatibleWith(testMark)
                && markFromCfgProposer.isCompatibleWith(testMark);
    }

    public CheckMark getCheckMark() {
        return checkMark;
    }

    public void setCheckMark(CheckMark checkMark) {
        this.checkMark = checkMark;
    }

    public MarkFromCfgProposer getMarkFromCfgProposer() {
        return markFromCfgProposer;
    }

    public void setMarkFromCfgProposer(MarkFromCfgProposer markFromCfgProposer) {
        this.markFromCfgProposer = markFromCfgProposer;
    }
}
