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
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;

public class Repeat extends MarkFromCfgProposer {

    // START BEAN
    @BeanField private MarkFromCfgProposer markFromCfgProposer;

    @BeanField private int maxIter = 20;
    // END BEAN

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return markFromCfgProposer.isCompatibleWith(testMark);
    }

    @Override
    public Optional<Mark> markFromCfg(Cfg cfg, ProposerContext context)
            throws ProposalAbnormalFailureException {

        context.addErrorLevel("MarkFromCfgProposerRepeat");

        for (int i = 0; i < maxIter; i++) {
            Optional<Mark> mark = markFromCfgProposer.markFromCfg(cfg, context);
            if (mark.isPresent()) {
                return mark;
            }
        }

        context.getErrorNode().add("maxIter reached");

        return Optional.empty();
    }

    public MarkFromCfgProposer getMarkFromCfgProposer() {
        return markFromCfgProposer;
    }

    public void setMarkFromCfgProposer(MarkFromCfgProposer markFromCfgProposer) {
        this.markFromCfgProposer = markFromCfgProposer;
    }

    public int getMaxIter() {
        return maxIter;
    }

    public void setMaxIter(int maxIter) {
        this.maxIter = maxIter;
    }
}
