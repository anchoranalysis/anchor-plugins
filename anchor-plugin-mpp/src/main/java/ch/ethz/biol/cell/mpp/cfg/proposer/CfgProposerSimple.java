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

package ch.ethz.biol.cell.mpp.cfg.proposer;

import cern.jet.random.Poisson;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.proposer.CfgProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;

public class CfgProposerSimple extends CfgProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MarkProposer markProposer;

    @BeanField @Getter @Setter
    private int numMarks = -1; // if positive, then we use this as the definitive number of marks
    // END BEAN PROPERTIES

    // Generates a random configuration
    //   * Number of objects is decided by a Poisson distribution
    //	 * The location and attributes of marks are uniformly distributed
    @Override
    public Optional<Cfg> propose(CfgGen cfgGen, ProposerContext context)
            throws ProposalAbnormalFailureException {
        return genInitRndCfgForNumPts(generateNumberPoints(cfgGen, context), cfgGen, context);
    }

    private int generateNumberPoints(CfgGen cfgGen, ProposerContext context) {
        if (numMarks > 0) {
            return numMarks;
        } else {
            Poisson distribution =
                    context.getRandomNumberGenerator()
                            .generatePoisson(
                                    cfgGen.getReferencePoissonIntensity()
                                            * context.getDimensions().getVolume());
            return distribution.nextInt();
        }
    }

    // Generates a random configuration for a given number of points
    //	 * The location and attributes of marks are uniformly distributed
    private Optional<Cfg> genInitRndCfgForNumPts(
            int numPoints, CfgGen cfgGen, ProposerContext context)
            throws ProposalAbnormalFailureException {

        Cfg cfg = new Cfg();

        for (int i = 0; i < numPoints; i++) {
            Mark mark = cfgGen.newTemplateMark();

            VoxelizedMarkMemo pmm = context.create(mark);

            // If the proposal fails, we don't bother trying another
            if (markProposer.propose(pmm, context)) {
                cfg.add(mark);
            }
        }

        return Optional.of(cfg);
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return this.markProposer.isCompatibleWith(testMark);
    }

    @Override
    public String getBeanDscr() {
        return getBeanName();
    }
}
