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

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.core.orientation.Orientation;
import org.anchoranalysis.mpp.bean.bound.Bound;
import org.anchoranalysis.mpp.bean.mark.bounds.RotationBounds3D;
import org.anchoranalysis.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.conic.Ellipsoid;
import org.anchoranalysis.mpp.mark.conic.RadiiRandomizer;
import org.anchoranalysis.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.mpp.proposer.ProposerContext;
import org.anchoranalysis.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.spatial.point.Point3d;

public class EllipsoidOrientationRadii extends MarkProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private RotationBounds3D rotationBounds = new RotationBounds3D();

    @BeanField @Getter @Setter private Bound radiusBound;
    // END BEAN PROPERTIES

    @Override
    public boolean propose(VoxelizedMarkMemo inputMark, ProposerContext context) {

        Ellipsoid mark = (Ellipsoid) inputMark.getMark();
        inputMark.reset();

        Orientation orientation =
                rotationBounds.randomOrientation(
                        context.getRandomNumberGenerator(), context.dimensions().resolution());

        Point3d radii =
                RadiiRandomizer.randomizeRadii(
                        radiusBound,
                        context.getRandomNumberGenerator(),
                        context.dimensions().resolution(),
                        true);

        mark.setMarksExplicit(mark.getPos(), orientation, radii);

        return true;
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof Ellipsoid;
    }

    @Override
    public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {
        return Optional.empty();
    }
}
