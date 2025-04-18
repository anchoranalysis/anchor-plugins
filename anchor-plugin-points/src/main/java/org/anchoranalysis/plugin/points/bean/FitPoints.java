/*-
 * #%L
 * anchor-plugin-points
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

package org.anchoranalysis.plugin.points.bean;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.mpp.bean.points.fitter.PointsFitter;
import org.anchoranalysis.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.mpp.bean.proposer.PointsProposer;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.mpp.proposer.ProposerContext;
import org.anchoranalysis.spatial.point.Point3d;
import org.anchoranalysis.spatial.point.Point3f;
import org.anchoranalysis.spatial.point.Point3i;
import org.anchoranalysis.spatial.point.PointConverter;

public class FitPoints extends MarkProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private PointsProposer pointsProposer;

    @BeanField @Getter @Setter private PointsFitter pointsFitter;

    @BeanField @Getter @Setter private boolean reportFitterErrors = true;
    // END BEAN PROPERTIES

    @SuppressWarnings("unused")
    private Logger logger;

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return pointsFitter.isCompatibleWith(testMark) && pointsProposer.isCompatibleWith(testMark);
    }

    @Override
    public boolean propose(VoxelizedMarkMemo inputMark, ProposerContext context)
            throws ProposalAbnormalFailureException {

        inputMark.reset();

        Point3d point = inputMark.getMark().centerPoint();

        try {
            Optional<List<Point3i>> points =
                    pointsProposer.propose(
                            point,
                            inputMark.getMark(),
                            context.dimensions(),
                            context.getRandomNumberGenerator(),
                            context.getErrorNode().add("pointsProposer"));

            if (!points.isPresent()) {
                return false;
            }

            // Now we create a list of point2d, and run the ellipse fitter on these
            List<Point3f> fitList =
                    FunctionalList.mapToList(points.get(), PointConverter::floatFromInt);

            pointsFitter.fit(fitList, inputMark.getMark(), context.dimensions());

        } catch (PointsFitterException | InsufficientPointsException e) {

            if (reportFitterErrors) {
                getLogger().errorReporter().recordError(FitPoints.class, e);
            }
            context.getErrorNode().add(e.toString());
            return false;
        }
        return true;
    }
}
