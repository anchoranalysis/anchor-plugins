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

package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.radii.RadiiProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkConic;
import org.anchoranalysis.anchor.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.orientation.Orientation;
import lombok.Getter;
import lombok.Setter;

/**
 * Proposes both an orientation and radii for a {@link MarkConic}
 *
 * @author Owen Feehan
 */
public class OrientationAndRadiiProposer extends MarkProposer {

    // BEAN PARAMETERS
    @BeanField @Getter @Setter private RadiiProposer radiiProposer = null;

    @BeanField @Getter @Setter private OrientationProposer orientationProposer = null;
    // END BEAN PARAMETERS
    public OrientationAndRadiiProposer(
            RadiiProposer radiiProposer, OrientationProposer orientationProposer) {
        this.radiiProposer = radiiProposer;
        this.orientationProposer = orientationProposer;
    }

    @Override
    public boolean propose(VoxelizedMarkMemo inputMark, ProposerContext context)
            throws ProposalAbnormalFailureException {

        MarkConic mark = (MarkConic) inputMark.getMark();

        Optional<Orientation> orientationNew =
                orientationProposer.propose(
                        inputMark.getMark(),
                        context.getDimensions(),
                        context.getRandomNumberGenerator());

        if (!orientationNew.isPresent()) {
            context.getErrorNode().add("orientationProposer returned null");
            return false;
        }
        // Now we try to randomzieMarks

        context = context.addErrorLevel("MarkRadiiProposer");

        Optional<Point3d> rad =
                radiiProposer.propose(
                        inputMark.getMark().centerPoint(),
                        context.getRandomNumberGenerator(),
                        context.getDimensions(),
                        orientationNew.get());

        if (!rad.isPresent()) {
            context.getErrorNode().add("radiiProposer returned null");
            return false;
        }

        mark.setMarksExplicit(inputMark.getMark().centerPoint(), orientationNew.get(), rad.get());
        inputMark.reset();
        return true;
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof MarkConic;
    }

    @Override
    public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {
        return Optional.empty();
    }
}
